package com.codetutor.samplehabittrackerapp

import androidx.room.withTransaction
import com.codetutor.samplehabittrackerapp.domain.model.CurrentStatus
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.rules.DecimalError
import com.codetutor.samplehabittrackerapp.domain.rules.NameError
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail

/**
 * Repository and reconcile cases shared by the host (Robolectric) and instrumented tiers. The
 * fixture's clock starts on Sunday 2026-10-11 10:00 UTC. Progress is written with SQL because the
 * SaveProgress command belongs to a later increment.
 */
object RepositoryScenarios {
    private val us = Locale.US
    private fun raw(
        name: String = "Meditation",
        unit: String = "minutes",
        target: String = "10",
        frequency: Frequency = Frequency.DAILY,
        weekdays: Int = 0,
    ) = RawHabitInput(name, unit, target, frequency, weekdays)

    private suspend fun RepositoryFixture.overview(): OverviewData = repository.observeOverview().first()
    private suspend fun RepositoryFixture.create(input: RawHabitInput = raw()) = repository.createHabit(input, us)
    private fun RepositoryFixture.at(iso: String) { clock.instant = Instant.parse(iso) }

    suspend fun createsDailyHabitAndOpensItsFirstPeriod(fx: RepositoryFixture) {
        assertTrue("a fresh database shows the no-habits state", fx.overview().isEmpty)

        val result = fx.create()
        assertTrue(result is CreateHabitResult.Created)
        result as CreateHabitResult.Created
        assertEquals(java.time.LocalDate.parse("2026-10-11"), result.firstStartLocalDate)

        val overview = fx.overview()
        assertFalse(overview.isEmpty)
        assertEquals(0L, overview.points)
        val habit = overview.habits.single()
        assertEquals("Meditation", habit.name)
        assertEquals("minutes", habit.unit)
        assertEquals(Frequency.DAILY, habit.frequency)
        assertEquals(1, habit.activeRun)
        assertEquals(0, habit.successStreak)
        assertFalse(habit.badgeEarned)
        assertEquals(0, habit.unacknowledgedMisses)
        assertNull(habit.lastResult)
        val status = habit.status as CurrentStatus.InProgress
        assertEquals(0, BigDecimal("10").compareTo(status.target))
        assertEquals(0, BigDecimal.ZERO.compareTo(status.progress))
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), status.endsAt)
    }

    suspend fun reconcileIsIdempotent(fx: RepositoryFixture) {
        fx.create()
        val first = fx.repository.reconcile()
        val second = fx.repository.reconcile()
        assertFalse(first.changedAnything)
        assertFalse(second.changedAnything)
        assertEquals(1L, fx.count("SELECT COUNT(*) FROM occurrences"))
    }

    suspend fun rejectsInvalidAndDuplicateInputWithoutSaving(fx: RepositoryFixture) {
        val invalid = fx.create(raw(name = "  ", target = "0")) as CreateHabitResult.InvalidInput
        assertEquals(NameError.EMPTY, invalid.errors.name)
        assertEquals(DecimalError.MUST_BE_POSITIVE, invalid.errors.target)
        assertEquals(0L, fx.count("SELECT COUNT(*) FROM habits"))
        assertTrue(fx.overview().isEmpty)

        assertTrue(fx.create(raw(name = "Meditation")) is CreateHabitResult.Created)
        assertEquals(CreateHabitResult.DuplicateName, fx.create(raw(name = "  meditation  ", unit = "reps")))
        assertEquals(1L, fx.count("SELECT COUNT(*) FROM habits"))

        // A pending-retirement habit still reserves its name; a retired one does not.
        fx.exec(
            "UPDATE habits SET status = 'PENDING_RETIREMENT', retirementDeadlineEpochMillis = ? WHERE habitId = 1",
            fx.clock.instant.toEpochMilli() + 10_000,
        )
        assertEquals(CreateHabitResult.DuplicateName, fx.create(raw(name = "MEDITATION")))
        fx.exec("UPDATE habits SET status = 'RETIRED', retiredAtEpochMillis = ? WHERE habitId = 1", 1L)
        assertTrue(fx.create(raw(name = "Meditation")) is CreateHabitResult.Created)
        assertEquals(2L, fx.count("SELECT COUNT(*) FROM habits"))
    }

    suspend fun weeklyCreatedOnSundayWaitsThenOpensOnMonday(fx: RepositoryFixture) {
        fx.create(raw(name = "Reading", unit = "pages", target = "20", frequency = Frequency.WEEKLY))
        val waiting = fx.overview().habits.single()
        assertEquals(0, waiting.activeRun)
        assertEquals(
            CurrentStatus.WaitingForFirstPeriod(Instant.parse("2026-10-12T00:00:00Z")),
            waiting.status,
        )

        fx.at("2026-10-12T00:00:00Z")
        val summary = fx.repository.reconcile()
        assertEquals(1, summary.plannedCount)
        val open = fx.overview().habits.single()
        assertEquals(1, open.activeRun)
        assertEquals(Instant.parse("2026-10-19T00:00:00Z"), (open.status as CurrentStatus.InProgress).endsAt)
    }

    suspend fun customHabitShowsNoOccurrenceOnAnUnselectedDay(fx: RepositoryFixture) {
        val mwf = Weekdays.maskOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        fx.create(raw(name = "Stretch", unit = "minutes", target = "5", frequency = Frequency.CUSTOM, weekdays = mwf))
        assertTrue(fx.overview().habits.single().status is CurrentStatus.WaitingForFirstPeriod)

        fx.at("2026-10-12T10:00:00Z") // Monday
        fx.repository.reconcile()
        assertTrue(fx.overview().habits.single().status is CurrentStatus.InProgress)

        fx.at("2026-10-13T10:00:00Z") // Tuesday: Monday finalized, Wednesday not started
        fx.repository.reconcile()
        val habit = fx.overview().habits.single()
        assertEquals(
            CurrentStatus.NoOccurrenceToday(Instant.parse("2026-10-14T00:00:00Z")),
            habit.status,
        )
        assertEquals(1, habit.activeRun)
        assertEquals(1, habit.unacknowledgedMisses)
    }

    suspend fun missFinalizesWithAnOutboxEventAndNoPointsAtZero(fx: RepositoryFixture) {
        fx.create()
        fx.at("2026-10-12T10:00:00Z")
        val summary = fx.repository.reconcile()
        assertEquals(1, summary.finalizedCount)
        assertEquals(1, summary.missCount)
        assertEquals(1, summary.plannedCount)

        assertEquals(listOf("MISS"), fx.strings("SELECT result FROM occurrences WHERE status = 'FINALIZED'"))
        assertEquals(listOf("PENDING"), fx.strings("SELECT deliveryState FROM notification_events"))
        val habit = fx.overview().habits.single()
        assertEquals(OccurrenceResult.MISS, habit.lastResult!!.result)
        assertEquals(0L, habit.lastResult!!.pointsEffect)
        assertEquals(2, habit.activeRun)
        assertEquals(0, habit.successStreak)
        assertEquals(1, habit.unacknowledgedMisses)
    }

    suspend fun successFinalizesWithTenPoints(fx: RepositoryFixture) {
        fx.create()
        fx.exec("UPDATE occurrences SET progress = '12' WHERE status = 'OPEN'")
        fx.at("2026-10-12T10:00:00Z")
        fx.repository.reconcile()

        val overview = fx.overview()
        assertEquals(10L, overview.points)
        val habit = overview.habits.single()
        assertEquals(OccurrenceResult.SUCCESS, habit.lastResult!!.result)
        assertEquals(10L, habit.lastResult!!.pointsEffect)
        assertEquals(2, habit.activeRun)
        assertEquals(1, habit.successStreak)
        assertEquals(0L, fx.count("SELECT COUNT(*) FROM notification_events"))
    }

    suspend fun consecutiveMissesDeductRoundedUpPercentOfTheBalance(fx: RepositoryFixture) {
        fx.exec("UPDATE app_state SET pointsBalance = 100")
        fx.create()
        fx.at("2026-10-13T10:00:00Z") // 11 Oct and 12 Oct both elapsed
        val summary = fx.repository.reconcile()
        assertEquals(2, summary.missCount)
        assertEquals(98L, fx.overview().points)
        assertEquals(listOf(-1L, -1L), fx.longs("SELECT pointsEffect FROM occurrences WHERE status = 'FINALIZED' ORDER BY occurrenceId"))
        assertEquals(2L, fx.count("SELECT COUNT(*) FROM notification_events"))
    }

    suspend fun catchUpAfterALongAbsenceFinalizesEveryElapsedPeriodOnce(fx: RepositoryFixture) {
        fx.create()
        fx.at("2026-11-10T08:00:00Z") // 30 days later
        val first = fx.repository.reconcile()
        assertEquals(30, first.finalizedCount)
        assertEquals(31, fx.overview().habits.single().activeRun)
        val second = fx.repository.reconcile()
        assertFalse(second.changedAnything)
        assertEquals(31L, fx.count("SELECT COUNT(*) FROM occurrences"))
        assertEquals(30L, fx.count("SELECT COUNT(*) FROM occurrences WHERE status = 'FINALIZED'"))
    }

    suspend fun gateIncrementsOncePerGatedTransactionAndRollsBackWithIt(fx: RepositoryFixture) {
        suspend fun version() = fx.count("SELECT version FROM reconciliation_lock")
        assertEquals(0L, version())
        fx.repository.reconcile()
        assertEquals(1L, version())
        fx.create()
        assertEquals(2L, version())
        fx.create(raw(name = "")) // rejected, but it still ran gated catch-up and committed it
        assertEquals(3L, version())

        val db = fx.provider.get()
        try {
            db.withTransaction {
                db.reconciliationLockDao().acquire()
                throw IllegalStateException("boom")
            }
            fail("the transaction should have failed")
        } catch (expected: IllegalStateException) {
            // the failure rolls the whole transaction back
        }
        assertEquals("rollback also undoes the gate increment", 3L, version())
    }

    suspend fun badgeIsAwardedOnceAfterEighteenSuccessfulDays(fx: RepositoryFixture) {
        fx.create()
        for (day in 0 until 18) {
            fx.exec("UPDATE occurrences SET progress = '10' WHERE status = 'OPEN'")
            fx.clock.instant = fx.clock.instant.plus(Duration.ofDays(1))
            fx.repository.reconcile()
            val expectedBadges = if (day == 17) 1L else 0L
            assertEquals("after day $day", expectedBadges, fx.count("SELECT COUNT(*) FROM badges"))
        }
        assertEquals(listOf("2026-10-28"), fx.strings("SELECT awardedForLocalDate FROM badges"))
        val habit = fx.overview().habits.single()
        assertTrue(habit.badgeEarned)
        assertEquals(18, habit.successStreak)
        assertEquals(180L, fx.overview().points)

        // A later miss never revokes or duplicates it.
        fx.clock.instant = fx.clock.instant.plus(Duration.ofDays(1))
        fx.repository.reconcile()
        assertEquals(1L, fx.count("SELECT COUNT(*) FROM badges"))
        assertTrue(fx.overview().habits.single().badgeEarned)
    }

    suspend fun nextBoundaryFollowsPeriodEndsStartsAndDeadlines(fx: RepositoryFixture) {
        assertNull(fx.repository.nextBoundary())

        fx.create(raw(name = "Reading", unit = "pages", target = "20", frequency = Frequency.WEEKLY))
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), fx.repository.nextBoundary()) // first period starts

        fx.create(raw(name = "Meditation")) // daily period ends tonight
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), fx.repository.nextBoundary())

        val deadline = fx.clock.instant.plusSeconds(10)
        fx.exec(
            "UPDATE habits SET status = 'PENDING_RETIREMENT', retirementDeadlineEpochMillis = ? WHERE name = 'Meditation'",
            deadline.toEpochMilli(),
        )
        assertEquals(deadline, fx.repository.nextBoundary())
    }

    suspend fun expiredRetirementCancelsTheFrozenPeriodWithoutScoring(fx: RepositoryFixture) {
        fx.create()
        val now = fx.clock.instant.toEpochMilli()
        fx.exec(
            "UPDATE habits SET status = 'PENDING_RETIREMENT', retirementDeadlineEpochMillis = ? WHERE habitId = 1",
            now + Duration.ofDays(10).toMillis(),
        )
        fx.at("2026-10-12T10:00:00Z") // the frozen period has elapsed, the deadline has not
        val frozen = fx.repository.reconcile()
        assertEquals(0, frozen.finalizedCount)
        assertEquals(listOf("OPEN"), fx.strings("SELECT status FROM occurrences"))
        assertEquals(1, fx.overview().pendingRetirements.size)

        fx.exec("UPDATE habits SET retirementDeadlineEpochMillis = ? WHERE habitId = 1", now + 10_000)
        val settled = fx.repository.reconcile()
        assertEquals(1, settled.retiredCount)
        assertEquals(listOf("CANCELLED"), fx.strings("SELECT result FROM occurrences"))
        assertEquals(listOf("RETIRED"), fx.strings("SELECT status FROM habits"))
        assertEquals(now + 10_000, fx.count("SELECT retiredAtEpochMillis FROM habits"))
        assertEquals(0L, fx.overview().points)
        assertEquals(0L, fx.count("SELECT COUNT(*) FROM notification_events"))
        assertTrue(fx.overview().isEmpty)
    }

    suspend fun overviewReEmitsAfterACommittedChange(fx: RepositoryFixture) = coroutineScope {
        val becomesNonEmpty = async { withTimeout(10_000) { fx.repository.observeOverview().first { !it.isEmpty } } }
        fx.create()
        assertEquals("Meditation", becomesNonEmpty.await().habits.single().name)
    }
}
