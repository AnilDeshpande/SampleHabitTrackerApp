@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import android.content.Context
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.core.time.BoundaryTicker
import com.codetutor.samplehabittrackerapp.core.time.TimeChangeSource
import com.codetutor.samplehabittrackerapp.core.work.TrackingCatchUp
import com.codetutor.samplehabittrackerapp.data.local.DatabaseConfig
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import com.codetutor.samplehabittrackerapp.domain.model.CurrentStatus
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import java.time.Instant
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * The two foreground-tracking regressions from the Increment 1 review, against the real repository, real
 * Room, the real [BoundaryTicker] and [TrackingCatchUp]. Only time and the time-change broadcast are
 * controlled. Shared by the host (Robolectric) and instrumented tiers.
 *
 * Each assertion is made on persisted rows or on what an already-active Overview subscription received.
 * Reopening the Overview, or calling the catch-up pipeline directly, would hide both defects.
 */
object TrackingScenarios {
    private val us = Locale.US

    /**
     * An Overview is already open on an empty database and the ticker is idle (nothing scheduled). The
     * user then creates the first habit through the real repository entry point and no other signal is
     * sent. Crossing the period's end must finalize it and the open Overview must show the result.
     */
    fun firstCreateFromAnEmptyOverviewRearmsTheTicker(context: Context) = runBlocking {
        // runTest would auto-advance virtual time whenever this body waits on Room's real threads, racing an
        // idle ticker forward. A hand-driven TestScope moves virtual time only when this test says so.
        val test = TestScope()
        with(test) {
        val clock = FakeClock(testScheduler) // 2026-01-05T09:00:00Z, UTC
        val provider = DatabaseProvider(context, DatabaseConfig(null), AppDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main))
        try {
            val real = RepositoryFixture.buildRepository(provider, clock)
            val lookups = AtomicInteger()
            val repository = object : HabitRepository by real {
                override suspend fun nextBoundary(): Instant? = real.nextBoundary().also { lookups.incrementAndGet() }
            }
            val catchUp = TrackingCatchUp(repository)
            val noTimeChanges = object : TimeChangeSource { override val events: Flow<Unit> = MutableSharedFlow() }
            val ticker = BoundaryTicker(backgroundScope, clock, repository, catchUp, noTimeChanges)

            val shown = mutableListOf<OverviewData>()
            backgroundScope.launch { repository.observeOverview().collect { shown += it } }
            ticker.start()
            assertTrue("the open Overview starts empty", awaitReal { shown.lastOrNull()?.isEmpty == true })
            assertTrue("the ticker looked, found nothing, and is idle", awaitReal { lookups.get() >= 1 })

            val created = repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), us)
            assertTrue(created is CreateHabitResult.Created)
            assertTrue("the first period opened with the habit", awaitReal { shown.lastOrNull()?.habits?.singleOrNull()?.status is CurrentStatus.InProgress })

            advanceTimeBy(15.hours + 1.minutes) // past 2026-01-06T00:00Z, the end of the first period
            val finalized = awaitReal {
                query(provider, "SELECT COUNT(*) FROM occurrences WHERE status = 'FINALIZED'") == 1L
            }
            val rows = query(provider, "SELECT COUNT(*) FROM occurrences WHERE status = 'FINALIZED'")
            assertEquals("the ticker finalized the first period at its boundary (finalized=$finalized)", 1L, rows)

            assertTrue("the open Overview received the result", awaitReal {
                shown.lastOrNull()?.habits?.singleOrNull()?.lastResult?.result == OccurrenceResult.MISS
            })
            val status = shown.last().habits.single().status
            assertTrue("the next period is open", status is CurrentStatus.InProgress)
            assertEquals(Instant.parse("2026-01-07T00:00:00Z"), (status as CurrentStatus.InProgress).endsAt)
        } finally {
            backgroundScope.cancel()
            provider.close()
        }
        }
    }

    /**
     * One Overview subscription stays active. The clock moves back before the open occurrence's stored
     * start; the system time-change broadcast reaches the real ticker, whose catch-up changes no habit or
     * occurrence row and updates only the gate. The same subscriber must drop the stale progress and show
     * [CurrentStatus.BetweenPeriods].
     */
    fun aClockMovedBackRefreshesTheActiveOverviewWhenOnlyTheGateChanges(context: Context) = runBlocking {
        val clock = ManualClock() // Sunday 2026-10-11T10:00:00Z
        val provider = DatabaseProvider(context, DatabaseConfig(null), AppDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main))
        val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        try {
            val repository = RepositoryFixture.buildRepository(provider, clock)
            val timeChanges = MutableSharedFlow<Unit>()
            val ticker = BoundaryTicker(
                appScope, clock, repository, TrackingCatchUp(repository),
                object : TimeChangeSource { override val events: Flow<Unit> = timeChanges },
            )
            repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), us)

            val shown = java.util.Collections.synchronizedList(mutableListOf<OverviewData>())
            val subscription = appScope.launch { repository.observeOverview().collect { shown += it } }
            assertTrue("progress is shown while the period is current", awaitWall { shown.lastOrNull()?.habits?.singleOrNull()?.status is CurrentStatus.InProgress })

            ticker.start()
            val tracked = "(SELECT group_concat(habitId || ':' || plannedThroughLocalDate, ',') FROM habits)" +
                " || '|' || (SELECT group_concat(occurrenceId || ':' || status || ':' || startInclusiveEpochMillis, ',') FROM occurrences)"
            val rowsBefore = text(provider, "SELECT $tracked")
            val gateBefore = query(provider, "SELECT version FROM reconciliation_lock")
            val emissionsBefore = shown.size

            clock.instant = Instant.parse("2026-10-10T23:00:00Z") // before the occurrence's stored start (2026-10-11T00:00Z)
            // The broadcast is only delivered to a subscribed collector, so repeat it until catch-up has run.
            awaitWall { timeChanges.emit(Unit); query(provider, "SELECT version FROM reconciliation_lock") > gateBefore }

            assertEquals("catch-up changed only the gate", rowsBefore, text(provider, "SELECT $tracked"))
            assertTrue("the gate advanced", query(provider, "SELECT version FROM reconciliation_lock") > gateBefore)

            val refreshed = awaitWall { shown.lastOrNull()?.habits?.singleOrNull()?.status is CurrentStatus.BetweenPeriods }
            val last = shown.last().habits.single().status
            assertTrue("the same subscription left the stale InProgress state (emissions: before=$emissionsBefore now=${shown.size}, refreshed=$refreshed, last=$last)", refreshed)
            assertEquals(Instant.parse("2026-10-11T00:00:00Z"), (last as CurrentStatus.BetweenPeriods).nextStartsAt)
            subscription.cancel()
            ticker.stop()
        } finally {
            appScope.cancel()
            provider.close()
        }
    }

    /** Pumps virtual time's queue while real Room threads run, then re-checks. Never throws on timeout. */
    private suspend fun TestScope.awaitReal(timeoutMs: Long = 5_000, condition: suspend () -> Boolean): Boolean {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000
        while (System.nanoTime() < deadline) {
            runCurrent()
            if (condition()) return true
            delay(10) // real time: Room's executor threads make progress meanwhile
        }
        runCurrent()
        return condition()
    }

    private suspend fun awaitWall(timeoutMs: Long = 10_000, condition: suspend () -> Boolean): Boolean {
        val deadline = System.nanoTime() + timeoutMs * 1_000_000
        while (System.nanoTime() < deadline) {
            if (condition()) return true
            delay(10)
        }
        return condition()
    }

    private suspend fun query(provider: DatabaseProvider, sql: String): Long =
        provider.get().openHelper.writableDatabase.query(sql).use { c -> c.moveToFirst(); c.getLong(0) }

    private suspend fun text(provider: DatabaseProvider, sql: String): String =
        provider.get().openHelper.writableDatabase.query(sql).use { c -> c.moveToFirst(); c.getString(0) }
}
