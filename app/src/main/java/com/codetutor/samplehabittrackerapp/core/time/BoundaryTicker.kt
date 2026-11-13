package com.codetutor.samplehabittrackerapp.core.time

import android.util.Log
import com.codetutor.samplehabittrackerapp.core.work.TrackingCatchUp
import com.codetutor.samplehabittrackerapp.di.ApplicationScope
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.cancellation.CancellationException

/**
 * Keeps the screen current while the app is in the foreground. It runs in the application scope only
 * while the process is STARTED: it sleeps until the next boundary (a period end, a retirement deadline
 * or a period start), runs catch-up, and re-arms. A system time, date or timezone change triggers an
 * immediate catch-up. This is a timeliness aid, not a scheduler: it promises no exact-midnight work,
 * and correctness stays with the command transactions.
 */
@Singleton
class BoundaryTicker @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    private val clock: Clock,
    private val repository: HabitRepository,
    private val catchUp: TrackingCatchUp,
    private val timeChanges: TimeChangeSource,
) {
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        job = scope.launch { loop() }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private suspend fun loop() = coroutineScope {
        val rearm = Channel<Unit>(Channel.CONFLATED)
        launch { catchUp.committed.collect { rearm.trySend(Unit) } }
        launch { timeChanges.events.collect { runCatchUp() } }

        while (isActive) {
            val wake = guarded { repository.nextBoundary() }
            if (wake == null) {
                rearm.receive() // nothing is scheduled; wait for a commit or a time change
                continue
            }
            val wait = (wake.toEpochMilli() - clock.now().toEpochMilli()).coerceAtLeast(0)
            val signalled = withTimeoutOrNull(wait) { rearm.receive(); true } == true
            if (signalled) continue // something committed or the clock moved: recompute the next boundary
            runCatchUp()
            // Never spin: if a boundary is still due after catching up, wait a moment before retrying.
            val next = guarded { repository.nextBoundary() }
            if (next != null && next.toEpochMilli() <= clock.now().toEpochMilli()) delay(MIN_REPEAT_MS)
        }
    }

    private suspend fun runCatchUp() {
        guarded { catchUp.run() } ?: delay(FAILURE_BACKOFF_MS)
    }

    private suspend fun <T> guarded(block: suspend () -> T): T? = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (failure: Throwable) {
        Log.w(TAG, "Boundary catch-up step failed; will retry", failure)
        null
    }

    companion object {
        private const val TAG = "BoundaryTicker"
        const val MIN_REPEAT_MS = 1_000L
        const val FAILURE_BACKOFF_MS = 30_000L
    }
}
