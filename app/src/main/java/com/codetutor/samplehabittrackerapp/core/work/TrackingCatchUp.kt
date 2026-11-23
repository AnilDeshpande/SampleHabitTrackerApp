package com.codetutor.samplehabittrackerapp.core.work

import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.repository.ReconcileSummary
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * The one foreground catch-up pipeline: the process observer, the overview-open request and the
 * boundary ticker all run through here, one at a time, on top of the database gate. Correctness
 * never depends on this (every command catches up under the gate); it only keeps the screen current.
 */
@Singleton
class TrackingCatchUp @Inject constructor(
    private val repository: HabitRepository,
) {
    private val mutex = Mutex()
    private val _committed = MutableSharedFlow<ReconcileSummary>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** Emits after each committed catch-up so the ticker can re-arm to the next boundary. */
    val committed: SharedFlow<ReconcileSummary> = _committed

    suspend fun run(): ReconcileSummary = mutex.withLock {
        repository.reconcile().also { _committed.tryEmit(it) }
    }
}
