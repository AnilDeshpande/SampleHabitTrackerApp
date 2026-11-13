@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import com.codetutor.samplehabittrackerapp.core.work.CommandRunner
import com.codetutor.samplehabittrackerapp.data.notification.NotificationPermissionPolicy
import com.codetutor.samplehabittrackerapp.data.preferences.NotificationPermissionStore
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.port.NotificationAvailability
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.repository.ReconcileSummary
import com.codetutor.samplehabittrackerapp.domain.rules.DecimalError
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputValidator
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import com.codetutor.samplehabittrackerapp.domain.rules.UnitError
import com.codetutor.samplehabittrackerapp.domain.rules.WeekdaysError
import com.codetutor.samplehabittrackerapp.feature.habitform.HabitFormEffect
import com.codetutor.samplehabittrackerapp.feature.habitform.HabitFormUiState
import com.codetutor.samplehabittrackerapp.feature.habitform.HabitFormViewModel
import com.codetutor.samplehabittrackerapp.feature.habitform.NameFieldError
import com.codetutor.samplehabittrackerapp.feature.habitform.StartPreview
import com.codetutor.samplehabittrackerapp.feature.habitform.SubmissionState
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Event-to-state tests for the create form against the real repository, Room (in memory), rules and
 * command runner. Only time, the notification-availability port and, in two tests, a gate/failure
 * wrapper around the repository are controlled.
 */
@RunWith(RobolectricTestRunner::class)
class HabitFormViewModelTest {
    private lateinit var fx: RepositoryFixture
    private lateinit var appScope: CoroutineScope
    private lateinit var uiScope: CoroutineScope
    private lateinit var runner: CommandRunner
    private var notificationsAllowed = false
    private val previousLocale = Locale.getDefault()

    @Before fun setUp() {
        Locale.setDefault(Locale.US)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fx = RepositoryFixture(ApplicationProvider.getApplicationContext())
        appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
        runner = CommandRunner(appScope)
    }

    @After fun tearDown() {
        uiScope.cancel()
        appScope.cancel()
        fx.close()
        Dispatchers.resetMain()
        Locale.setDefault(previousLocale)
    }

    private fun policy(): NotificationPermissionPolicy {
        val file = File(ApplicationProvider.getApplicationContext<android.content.Context>().cacheDir, "policy-${UUID.randomUUID()}.preferences_pb")
        val store = NotificationPermissionStore(PreferenceDataStoreFactory.create(scope = appScope) { file })
        return NotificationPermissionPolicy(store, object : NotificationAvailability {
            override fun canPostMissedTargetNotifications() = notificationsAllowed
        })
    }

    private fun viewModel(
        saved: Map<String, Any?> = emptyMap(),
        repository: HabitRepository = fx.repository,
    ) = HabitFormViewModel(
        savedState = SavedStateHandle(saved),
        repository = repository,
        runner = runner,
        validator = HabitInputValidator(),
        planner = OccurrencePlanner(),
        clock = fx.clock,
        permissionPolicy = policy(),
    )

    /** Keeps the state flow subscribed (it is WhileSubscribed) and returns the latest state accessor. */
    private fun HabitFormViewModel.observe(): () -> HabitFormUiState {
        val job: Job = uiScope.launch { uiState.collect { } }
        job.invokeOnCompletion { }
        return { uiState.value }
    }

    private fun HabitFormViewModel.fill(name: String = "Meditation", unit: String = "minutes", target: String = "10") {
        onNameChange(name); onUnitChange(unit); onTargetChange(target)
    }

    private suspend fun HabitFormViewModel.nextEffect(): HabitFormEffect = withTimeout(15_000) { effects.first() }

    private suspend fun awaitState(state: () -> HabitFormUiState, predicate: (HabitFormUiState) -> Boolean): HabitFormUiState =
        withTimeout(15_000) { while (!predicate(state())) kotlinx.coroutines.delay(10); state() }

    @Test fun startsEmptyCleanAndPreviewsToday() {
        val vm = viewModel(); val state = vm.observe()
        assertEquals("", state().name)
        assertFalse(state().isDirty)
        assertNull(state().nameError)
        assertEquals(StartPreview.Today, state().startsOn)
        assertEquals(SubmissionState.Idle, state().submission)
    }

    @Test fun typingIsWrittenToSavedStateImmediatelyAndMarksTheFormDirty() {
        val handle = SavedStateHandle()
        val vm = HabitFormViewModel(handle, fx.repository, runner, HabitInputValidator(), OccurrencePlanner(), fx.clock, policy())
        val state = vm.observe()
        vm.onNameChange("Med")
        assertEquals("Med", handle.get<String>(HabitFormViewModel.KEY_NAME))
        assertEquals("Med", state().name)
        assertTrue(state().isDirty)
    }

    @Test fun aRestoredDraftKeepsItsRawTextAndRevalidatesIt() {
        val vm = viewModel(
            mapOf(
                HabitFormViewModel.KEY_NAME to "Meditation",
                HabitFormViewModel.KEY_UNIT to "minutes",
                HabitFormViewModel.KEY_TARGET to "1,2.3",
                HabitFormViewModel.KEY_FREQUENCY to Frequency.CUSTOM.name,
                HabitFormViewModel.KEY_WEEKDAYS to 21,
            ),
        )
        val state = vm.observe()
        assertEquals("1,2.3", state().target) // exact raw text, never normalized
        assertEquals(Frequency.CUSTOM, state().frequency)
        assertEquals(21, state().weekdays)
        assertEquals("the error is recomputed, not saved", DecimalError.NOT_A_NUMBER, state().targetError)
        assertNull(state().nameError)
        assertEquals("restoration inserts no habit", 0L, runBlocking { fx.count("SELECT COUNT(*) FROM habits") })
    }

    @Test fun savingAnInvalidFormShowsEveryErrorAndRunsNoCommand() = runBlocking {
        val vm = viewModel(); val state = vm.observe()
        assertNull("no errors before the first Save", state().targetError)
        vm.onSave()
        assertEquals(NameFieldError.Empty, state().nameError)
        assertEquals(UnitError.EMPTY, state().unitError)
        assertEquals(DecimalError.EMPTY, state().targetError)
        assertEquals(0L, fx.count("SELECT COUNT(*) FROM habits"))
        assertEquals(0L, fx.count("SELECT version FROM reconciliation_lock"))
    }

    @Test fun savingAValidFormCommitsAndAsksForNotificationPermissionOnce() = runBlocking {
        val vm = viewModel(); vm.observe()
        vm.fill()
        vm.onSave()
        assertEquals(HabitFormEffect.Saved(requestNotificationPermission = true), vm.nextEffect())
        assertEquals(1L, fx.count("SELECT COUNT(*) FROM habits"))
        assertTrue(fx.repository.observeOverview().first().habits.single().name == "Meditation")
    }

    @Test fun neverAsksForPermissionAgainOrWhenAlreadyAllowed() = runBlocking {
        notificationsAllowed = true
        val vm = viewModel(); vm.observe()
        vm.fill(); vm.onSave()
        assertEquals(HabitFormEffect.Saved(requestNotificationPermission = false), vm.nextEffect())
    }

    @Test fun aDuplicateNameIsReportedOnTheNameFieldAndNothingIsSavedTwice() = runBlocking {
        fx.repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), Locale.US)
        val vm = viewModel(); val state = vm.observe()
        vm.fill(name = "  meditation  ", unit = "reps", target = "5")
        vm.onSave()
        awaitState(state) { it.nameError is NameFieldError.Duplicate }
        assertEquals(NameFieldError.Duplicate("meditation"), state().nameError)
        assertEquals(1L, fx.count("SELECT COUNT(*) FROM habits"))
        assertEquals("input is retained", "reps", state().unit)
        vm.onNameChange("Reading")
        assertNull("editing the name clears the duplicate error", state().nameError)
    }

    @Test fun backOnACleanFormLeavesAndOnADirtyFormAsksFirst() = runBlocking {
        val clean = viewModel(); clean.observe()
        clean.onBack()
        assertEquals(HabitFormEffect.NavigateBack, clean.nextEffect())

        val dirty = viewModel(); val state = dirty.observe()
        dirty.onNameChange("Med")
        dirty.onBack()
        assertTrue(state().showDiscardDialog)
        dirty.onKeepEditing()
        assertFalse(state().showDiscardDialog)
        assertEquals("keep editing retains the raw values", "Med", state().name)
        dirty.onBack()
        dirty.onDiscard()
        assertFalse(state().showDiscardDialog)
        assertEquals(HabitFormEffect.NavigateBack, dirty.nextEffect())
    }

    @Test fun whileSavingTheSaveIsBlockedAndASecondTapDoesNothing() = runBlocking {
        val gate = CompletableDeferred<Unit>()
        val gated = object : HabitRepository by fx.repository {
            override suspend fun createHabit(raw: RawHabitInput, locale: Locale): CreateHabitResult {
                gate.await()
                return fx.repository.createHabit(raw, locale)
            }
        }
        val vm = viewModel(repository = gated); val state = vm.observe()
        vm.fill(); vm.onSave()
        awaitState(state) { it.isSaving }
        vm.onSave() // a duplicate tap while one is in flight
        gate.complete(Unit)
        assertEquals(HabitFormEffect.Saved(true), vm.nextEffect())
        assertEquals("exactly one habit was created", 1L, fx.count("SELECT COUNT(*) FROM habits"))
        assertFalse("the key is released once the save commits", awaitState(state) { !it.isSaving }.isSaving)
    }

    @Test fun aFailedSaveKeepsTheInputAndARetrySucceeds() = runBlocking {
        var failNext = true
        val flaky = object : HabitRepository by fx.repository {
            override suspend fun createHabit(raw: RawHabitInput, locale: Locale): CreateHabitResult {
                if (failNext) { failNext = false; error("database unavailable") }
                return fx.repository.createHabit(raw, locale)
            }
        }
        val vm = viewModel(repository = flaky); val state = vm.observe()
        vm.fill(); vm.onSave()
        awaitState(state) { it.submission == SubmissionState.Failed }
        assertEquals("Meditation", state().name)
        assertEquals(0L, fx.count("SELECT COUNT(*) FROM habits"))

        vm.onSave() // Retry
        assertTrue(vm.nextEffect() is HabitFormEffect.Saved)
        assertEquals(1L, fx.count("SELECT COUNT(*) FROM habits"))
    }

    @Test fun theStartPreviewFollowsTheFrequencyAndWeekdays() {
        val vm = viewModel(); val state = vm.observe()
        // The fixture clock is Sunday 2026-10-11.
        vm.onFrequencyChange(Frequency.WEEKLY)
        assertEquals(StartPreview.On(LocalDate.parse("2026-10-12")), state().startsOn)
        vm.onFrequencyChange(Frequency.MONTHLY)
        assertEquals(StartPreview.On(LocalDate.parse("2026-11-01")), state().startsOn)
        vm.onFrequencyChange(Frequency.CUSTOM)
        assertNull("no preview until a weekday is chosen", state().startsOn)
        vm.onWeekdayToggle(DayOfWeek.MONDAY); vm.onWeekdayToggle(DayOfWeek.WEDNESDAY); vm.onWeekdayToggle(DayOfWeek.FRIDAY)
        assertEquals(21, state().weekdays)
        assertEquals(StartPreview.On(LocalDate.parse("2026-10-12")), state().startsOn)
        vm.onWeekdayToggle(DayOfWeek.WEDNESDAY)
        assertEquals(17, state().weekdays)
        vm.onFrequencyChange(Frequency.DAILY)
        assertEquals("a non-Custom habit stores no weekdays", 0, state().weekdays)
        assertEquals(StartPreview.Today, state().startsOn)
    }

    @Test fun customWithNoWeekdayReportsTheWeekdayErrorAfterASaveAttempt() {
        val vm = viewModel(); val state = vm.observe()
        vm.fill(); vm.onFrequencyChange(Frequency.CUSTOM)
        vm.onSave()
        assertEquals(WeekdaysError.NONE_SELECTED, state().weekdaysError)
        assertEquals(0L, runBlocking { fx.count("SELECT COUNT(*) FROM habits") })
    }

    @Test fun reconcileSummaryTypeIsAvailableToTheScreenLayerTests() {
        assertEquals(0, ReconcileSummary(0, 0, 0, 0).plannedCount)
        assertEquals(OverviewData(0, emptyList(), emptyList()).isEmpty, true)
    }
}
