package com.codetutor.samplehabittrackerapp.core.work

import com.codetutor.samplehabittrackerapp.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

sealed interface CommandResult<out R> {
    data class Success<R>(val value: R) : CommandResult<R>

    /** An ordinary failure. The command's transaction rolled back, so nothing was partially saved. */
    data class Failure(val cause: Throwable) : CommandResult<Nothing>
}

sealed interface Submission<out R> {
    /** The command was admitted and is running in the application scope. Await [ticket] to see its result. */
    data class Accepted<R>(val ticket: Deferred<CommandResult<R>>) : Submission<R>

    /** A command with this key is already in flight, so this one was rejected before it started. */
    data object Busy : Submission<Nothing>
}

/**
 * Runs accepted commands in the application scope so they outlive the screen that submitted them
 * (LLD: Concurrency and work lifetime).
 *
 * - [submit] atomically reserves the admission key, then starts the command as an application job
 *   before returning. A duplicate in-flight key is rejected without running anything.
 * - The ticket is a plain [Deferred]; a ViewModel awaits it from `viewModelScope`. Cancelling that
 *   wait cancels neither the command nor its effects, because both are children of the application
 *   scope, not of the waiter.
 * - The key is released when the transaction commits, fails or is rejected, before any effect runs,
 *   so slow effects never keep a Save button disabled.
 * - Effects run only after a successful transaction, as a separate job holding no key. An effect
 *   failure never turns a committed result into a failed one.
 */
@Singleton
class CommandRunner @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
) {
    private val busyKeys = MutableStateFlow<Set<String>>(emptySet())

    /** Whether a command with [key] is in flight. Survives screen recreation: it is process-wide state. */
    fun isBusy(key: String): Flow<Boolean> = busyKeys.map { key in it }.distinctUntilChanged()

    fun <R> submit(
        key: String,
        afterCommit: (suspend (R) -> Unit)? = null,
        command: suspend () -> R,
    ): Submission<R> {
        if (!reserve(key)) return Submission.Busy
        val ticket = scope.async {
            val outcome: CommandResult<R> = try {
                CommandResult.Success(command())
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (failure: Throwable) {
                CommandResult.Failure(failure)
            } finally {
                release(key)
            }
            if (outcome is CommandResult.Success && afterCommit != null) {
                scope.launch {
                    try {
                        afterCommit(outcome.value)
                    } catch (cancellation: CancellationException) {
                        throw cancellation
                    } catch (ignored: Throwable) {
                        // The committed result stands; effect failures are recovered by durable retry paths.
                    }
                }
            }
            outcome
        }
        return Submission.Accepted(ticket)
    }

    private fun reserve(key: String): Boolean {
        while (true) {
            val current = busyKeys.value
            if (key in current) return false
            if (busyKeys.compareAndSet(current, current + key)) return true
        }
    }

    private fun release(key: String) {
        while (true) {
            val current = busyKeys.value
            if (busyKeys.compareAndSet(current, current - key)) return
        }
    }

    companion object {
        const val CREATE_HABIT_KEY = "habit.create"
    }
}
