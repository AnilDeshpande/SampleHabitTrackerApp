@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.core.work.CommandResult
import com.codetutor.samplehabittrackerapp.core.work.CommandRunner
import com.codetutor.samplehabittrackerapp.core.work.Submission
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class CommandRunnerTest {
    private fun TestScope.runner(): CommandRunner =
        CommandRunner(CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler)))

    private fun <R> Submission<R>.ticket() = (this as Submission.Accepted<R>).ticket

    @Test fun acceptedCommandReturnsItsResultThroughTheTicket() = runTest {
        val runner = runner()
        val result = runner.submit("k") { 42 }.ticket().await()
        assertEquals(CommandResult.Success(42), result)
    }

    @Test fun aDuplicateInFlightKeyIsRejectedBeforeItRuns() = runTest {
        val runner = runner()
        val gate = CompletableDeferred<Unit>()
        var secondRan = false
        val first = runner.submit("habit.create") { gate.await(); "first" }
        runCurrent()
        assertTrue(first is Submission.Accepted)

        val second = runner.submit("habit.create") { secondRan = true; "second" }
        assertSame(Submission.Busy, second)

        gate.complete(Unit)
        assertEquals(CommandResult.Success("first"), first.ticket().await())
        assertFalse(secondRan)
    }

    @Test fun differentKeysRunConcurrently() = runTest {
        val runner = runner()
        val gate = CompletableDeferred<Unit>()
        val a = runner.submit("a") { gate.await(); 1 }
        val b = runner.submit("b") { gate.await(); 2 }
        assertTrue(a is Submission.Accepted && b is Submission.Accepted)
        gate.complete(Unit)
        assertEquals(1, (a.ticket().await() as CommandResult.Success).value)
        assertEquals(2, (b.ticket().await() as CommandResult.Success).value)
    }

    @Test fun keyIsReleasedAfterSuccessAndAfterFailure() = runTest {
        val runner = runner()
        runner.submit("k") { 1 }.ticket().await()
        assertTrue("released after success", runner.submit("k") { 2 } is Submission.Accepted)
        advanceUntilIdle()

        val failure = runner.submit<Int>("k") { error("boom") }.ticket().await()
        assertTrue(failure is CommandResult.Failure)
        assertEquals("boom", (failure as CommandResult.Failure).cause.message)
        assertTrue("released after failure", runner.submit("k") { 3 } is Submission.Accepted)
    }

    @Test fun busyStateIsObservableAndClearsAfterCommit() = runTest {
        val runner = runner()
        val gate = CompletableDeferred<Unit>()
        assertFalse(runner.isBusy("k").first())
        val ticket = runner.submit("k") { gate.await() }.ticket()
        runCurrent()
        assertTrue("a reopened screen sees the in-flight command", runner.isBusy("k").first())
        gate.complete(Unit)
        ticket.await()
        assertFalse(runner.isBusy("k").first())
    }

    @Test fun cancellingTheWaiterDoesNotCancelTheCommand() = runTest {
        val runner = runner()
        val gate = CompletableDeferred<Unit>()
        var committed = false
        val ticket = runner.submit("k") { gate.await(); committed = true; "done" }.ticket()
        val waiterScope = CoroutineScope(SupervisorJob() + StandardTestDispatcher(testScheduler))
        val waiter = waiterScope.launch { ticket.await() }
        runCurrent()

        waiterScope.cancel() // the screen is gone
        runCurrent()
        assertTrue(waiter.isCancelled)

        gate.complete(Unit)
        advanceUntilIdle()
        assertTrue("the accepted command still committed", committed)
        assertEquals(CommandResult.Success("done"), ticket.await())
        assertFalse("and its key was released", runner.isBusy("k").first())
    }

    @Test fun effectRunsOnlyAfterCommitAndAfterTheKeyIsReleased() = runTest {
        val runner = runner()
        var busyWhenEffectRan: Boolean? = null
        val effectValues = mutableListOf<Int>()
        runner.submit("k", afterCommit = { value ->
            busyWhenEffectRan = runner.isBusy("k").first()
            effectValues += value
        }) { 7 }.ticket().await()
        advanceUntilIdle()
        assertEquals(listOf(7), effectValues)
        assertEquals("a slow effect never keeps the key busy", false, busyWhenEffectRan)
    }

    @Test fun noEffectRunsWhenTheCommandFails() = runTest {
        val runner = runner()
        var effectRan = false
        runner.submit<Int>("k", afterCommit = { effectRan = true }) { error("rolled back") }.ticket().await()
        advanceUntilIdle()
        assertFalse(effectRan)
    }

    @Test fun anEffectFailureNeverChangesTheCommittedResult() = runTest {
        val runner = runner()
        val result = runner.submit("k", afterCommit = { error("notification failed") }) { "saved" }.ticket().await()
        advanceUntilIdle()
        assertEquals(CommandResult.Success("saved"), result)
        assertTrue("and later commands are unaffected", runner.submit("k") { 1 } is Submission.Accepted)
    }
}
