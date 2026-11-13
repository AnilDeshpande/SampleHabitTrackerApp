@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.codetutor.samplehabittrackerapp.core.lifecycle.ForegroundCoordinator
import com.codetutor.samplehabittrackerapp.core.time.BoundaryTicker
import com.codetutor.samplehabittrackerapp.core.time.TimeChangeSource
import com.codetutor.samplehabittrackerapp.core.work.TrackingCatchUp
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.repository.ReconcileSummary
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import java.time.Duration
import java.time.Instant
import java.util.Locale
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * The ticker and the foreground observer under virtual time. The repository here is a scripted fake:
 * these tests are about scheduling only (when catch-up runs), and every rule and SQL claim is made
 * against the real repository elsewhere.
 */
@RunWith(RobolectricTestRunner::class)
class BoundaryTickerTest {

    private class ScriptedRepository(var boundary: Instant?) : HabitRepository {
        var reconcileCalls = 0
        var onReconcile: (() -> Unit)? = null
        var failNextReconcile = false

        override fun observeOverview(): Flow<OverviewData> = emptyFlow()
        override suspend fun createHabit(raw: RawHabitInput, locale: Locale): CreateHabitResult = error("not used")
        override suspend fun nextBoundary(): Instant? = boundary
        override suspend fun reconcile(): ReconcileSummary {
            reconcileCalls++
            if (failNextReconcile) { failNextReconcile = false; error("database busy") }
            onReconcile?.invoke()
            return ReconcileSummary(0, 0, 0, 0)
        }
    }

    private class Harness(val test: TestScope, boundary: Instant?) {
        val clock = FakeClock(test.testScheduler)
        val repository = ScriptedRepository(boundary)
        val timeChanges = MutableSharedFlow<Unit>()
        val catchUp = TrackingCatchUp(repository)
        val ticker = BoundaryTicker(
            scope = test.backgroundScope,
            clock = clock,
            repository = repository,
            catchUp = catchUp,
            timeChanges = object : TimeChangeSource { override val events: Flow<Unit> = timeChanges },
        )
        val coordinator = ForegroundCoordinator(test.backgroundScope, catchUp, ticker)
    }

    private fun TestScope.harness(boundaryAfter: Duration? = Duration.ofHours(1)): Harness {
        val base = Instant.parse("2026-01-05T09:00:00Z") // FakeClock's base
        return Harness(this, boundaryAfter?.let { base.plus(it) })
    }

    @Test fun wakesAtTheNextBoundaryRunsCatchUpAndRearms() = runTest {
        val h = harness()
        h.repository.onReconcile = { h.repository.boundary = h.clock.now().plus(Duration.ofHours(1)) }
        h.ticker.start()
        runCurrent()

        advanceTimeBy(59.minutes); runCurrent()
        assertEquals("not yet", 0, h.repository.reconcileCalls)
        advanceTimeBy(2.minutes); runCurrent()
        assertEquals("woke at the boundary", 1, h.repository.reconcileCalls)

        advanceTimeBy(61.minutes); runCurrent()
        assertEquals("re-armed to the next boundary", 2, h.repository.reconcileCalls)
    }

    @Test fun aSystemTimeChangeRunsCatchUpImmediately() = runTest {
        val h = harness()
        h.ticker.start()
        runCurrent()
        h.timeChanges.emit(Unit)
        runCurrent()
        assertEquals(1, h.repository.reconcileCalls)
    }

    @Test fun stoppingTheTickerStopsWakeUps() = runTest {
        val h = harness()
        h.ticker.start()
        runCurrent()
        h.ticker.stop()
        advanceTimeBy(600.minutes); runCurrent()
        assertEquals(0, h.repository.reconcileCalls)
    }

    @Test fun withNothingScheduledItWaitsForACommitThenSchedules() = runTest {
        val h = harness(boundaryAfter = null)
        h.ticker.start()
        advanceTimeBy(5.minutes); runCurrent()
        assertEquals(0, h.repository.reconcileCalls)

        // Something commits (for example a habit was created): the ticker re-arms to the new boundary.
        h.repository.boundary = h.clock.now().plus(Duration.ofMinutes(30))
        h.catchUp.run()
        assertEquals(1, h.repository.reconcileCalls)
        h.repository.onReconcile = { h.repository.boundary = null } // the wake-up clears the boundary

        advanceTimeBy(29.minutes); runCurrent()
        assertEquals("not yet", 1, h.repository.reconcileCalls)
        advanceTimeBy(2.minutes); runCurrent()
        assertEquals("woke at the new boundary", 2, h.repository.reconcileCalls)
    }

    @Test fun aBoundaryThatNeverClearsDoesNotSpin() = runTest {
        val h = harness(boundaryAfter = Duration.ofHours(-1)) // already due, and reconcile never clears it
        h.ticker.start()
        advanceTimeBy(10.seconds); runCurrent()
        assertTrue("catch-up ran at least once", h.repository.reconcileCalls >= 1)
        assertTrue("but at most about once per second, got ${h.repository.reconcileCalls}", h.repository.reconcileCalls <= 12)
    }

    @Test fun aFailedCatchUpIsRetriedLaterNotLost() = runTest {
        val h = harness(boundaryAfter = Duration.ofMinutes(10))
        h.repository.failNextReconcile = true
        h.repository.onReconcile = { h.repository.boundary = h.clock.now().plus(Duration.ofHours(1)) }
        h.ticker.start()
        advanceTimeBy(10.minutes + 10.seconds); runCurrent()
        assertEquals("first attempt failed", 1, h.repository.reconcileCalls)
        advanceTimeBy(BoundaryTicker.FAILURE_BACKOFF_MS + 1_000); runCurrent()
        assertTrue("retried after the backoff", h.repository.reconcileCalls >= 2)
    }

    // ---- ForegroundCoordinator ----

    private class FakeProcess : LifecycleOwner {
        val registry = LifecycleRegistry.createUnsafe(this)
        override val lifecycle: Lifecycle get() = registry
    }

    @Test fun processStartRunsCatchUpAndStartsTheTickerAndStopStopsIt() = runTest {
        val h = harness()
        val process = FakeProcess()
        h.coordinator.install(process.lifecycle)

        process.registry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        process.registry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        runCurrent()
        assertEquals("catch-up on foreground", 1, h.repository.reconcileCalls)

        advanceTimeBy(61.minutes); runCurrent()
        assertTrue("the ticker is running while started", h.repository.reconcileCalls >= 2)

        val callsAtStop = h.repository.reconcileCalls
        process.registry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        h.repository.boundary = h.clock.now().plus(Duration.ofMinutes(5))
        advanceTimeBy(30.minutes); runCurrent()
        assertEquals("no wake-ups after the process stops", callsAtStop, h.repository.reconcileCalls)
    }

    @Test fun anOverviewOpenRequestUsesTheSamePipeline() = runTest {
        val h = harness()
        h.coordinator.requestCatchUp()
        runCurrent()
        assertEquals(1, h.repository.reconcileCalls)
    }
}
