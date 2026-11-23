@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.core.lifecycle.ForegroundCoordinator
import com.codetutor.samplehabittrackerapp.core.time.BoundaryTicker
import com.codetutor.samplehabittrackerapp.core.work.CommandResult
import com.codetutor.samplehabittrackerapp.core.work.CommandRunner
import com.codetutor.samplehabittrackerapp.core.work.Submission
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import com.codetutor.samplehabittrackerapp.di.ApplicationScope
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The production Hilt graph with the test time/storage modules: the coordination layer really wires up. */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class CoordinationGraphTest {
    @get:Rule val hilt = HiltAndroidRule(this)

    @Inject lateinit var scheduler: TestCoroutineScheduler
    @Inject lateinit var runner: CommandRunner
    @Inject lateinit var repository: HabitRepository
    @Inject lateinit var coordinator: ForegroundCoordinator
    @Inject lateinit var ticker: BoundaryTicker
    @Inject lateinit var provider: DatabaseProvider
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    @Before fun inject() = hilt.inject()

    @After fun tearDown() {
        appScope.coroutineContext[Job]?.cancel()
        provider.close()
    }

    private val input = RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0)

    @Test fun createdHabitCommitsEvenIfTheSubmittingScreenIsGone() = runTest(scheduler) {
        val gate = CompletableDeferred<Unit>()
        val ticket = (runner.submit(CommandRunner.CREATE_HABIT_KEY) {
            gate.await()
            repository.createHabit(input, Locale.US)
        } as Submission.Accepted).ticket

        val screen = CoroutineScope(SupervisorJob() + StandardTestDispatcher(scheduler))
        val waiter = screen.launch { ticket.await() }
        scheduler.runCurrent()
        screen.cancel() // the user left the form while the save was in flight
        scheduler.runCurrent()
        assertTrue(waiter.isCancelled)

        gate.complete(Unit)
        val saved = repository.observeOverview().first { !it.isEmpty } // runTest's own real-time limit applies
        assertEquals("Meditation", saved.habits.single().name)
        assertTrue(ticket.await() is CommandResult.Success)
        assertTrue((ticket.await() as CommandResult.Success).value is CreateHabitResult.Created)
    }

    @Test fun aSecondCreateWhileOneIsInFlightIsRejected() = runTest(scheduler) {
        val gate = CompletableDeferred<Unit>()
        val first = runner.submit(CommandRunner.CREATE_HABIT_KEY) { gate.await(); repository.createHabit(input, Locale.US) }
        assertTrue(first is Submission.Accepted)
        assertEquals(Submission.Busy, runner.submit(CommandRunner.CREATE_HABIT_KEY) { repository.createHabit(input, Locale.US) })
        gate.complete(Unit)
        (first as Submission.Accepted).ticket.await()
        // Once the first transaction finished, the slot is free and the duplicate-name rule decides.
        val again = (runner.submit(CommandRunner.CREATE_HABIT_KEY) { repository.createHabit(input, Locale.US) } as Submission.Accepted).ticket.await()
        assertEquals(CreateHabitResult.DuplicateName, (again as CommandResult.Success).value)
    }

    @Test fun coordinationObjectsAreSingletons() {
        assertTrue(coordinator === coordinator && ticker === ticker)
    }
}
