package com.codetutor.samplehabittrackerapp.feature.habitform

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.core.time.snapshot
import com.codetutor.samplehabittrackerapp.core.work.CommandResult
import com.codetutor.samplehabittrackerapp.core.work.CommandRunner
import com.codetutor.samplehabittrackerapp.core.work.Submission
import com.codetutor.samplehabittrackerapp.data.notification.NotificationPermissionPolicy
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.rules.FieldResult
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputResult
import com.codetutor.samplehabittrackerapp.domain.rules.HabitInputValidator
import com.codetutor.samplehabittrackerapp.domain.rules.HabitSchedule
import com.codetutor.samplehabittrackerapp.domain.rules.NameError
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Create-habit form. The draft's raw text lives in [SavedStateHandle] under the LLD's keys and is
 * written on every input event, so it survives rotation and system process death and an
 * intentionally empty field is never replaced by a default. Validation is derived from that raw
 * text and is never saved.
 *
 * Saving goes through the application-scoped [CommandRunner], so leaving the screen cancels only the
 * wait: an accepted save still commits. Field errors appear after the first Save attempt, and on a
 * restored draft (the validator runs again on the restored text).
 */
@HiltViewModel
class HabitFormViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    private val repository: HabitRepository,
    private val runner: CommandRunner,
    private val validator: HabitInputValidator,
    private val planner: OccurrencePlanner,
    private val clock: Clock,
    private val permissionPolicy: NotificationPermissionPolicy,
) : ViewModel() {

    private data class Draft(
        val name: String,
        val unit: String,
        val target: String,
        val frequency: Frequency,
        val weekdays: Int,
    )

    /** State that belongs to this screen instance only: never saved, rebuilt after recreation. */
    private data class Transient(
        val showErrors: Boolean,
        val duplicateName: String?,
        val failed: Boolean,
        val discardDialog: Boolean,
    )

    // Read before any getStateFlow, so an absent key is told apart from an intentionally empty one.
    private val restoredFromSavedState = DRAFT_KEYS.any { savedState.contains(it) }

    private val showErrors = MutableStateFlow(restoredFromSavedState)
    private val duplicateName = MutableStateFlow<String?>(null)
    private val failed = MutableStateFlow(false)
    private val discardDialog = MutableStateFlow(false)

    private val _effects = Channel<HabitFormEffect>(Channel.BUFFERED)
    val effects: Flow<HabitFormEffect> = _effects.receiveAsFlow()

    private val draftFlow: Flow<Draft> = combine(
        savedState.getStateFlow(KEY_NAME, ""),
        savedState.getStateFlow(KEY_UNIT, ""),
        savedState.getStateFlow(KEY_TARGET, ""),
        savedState.getStateFlow(KEY_FREQUENCY, Frequency.DAILY.name),
        savedState.getStateFlow(KEY_WEEKDAYS, Weekdays.NONE),
    ) { name, unit, target, frequency, weekdays ->
        // Two separate writes change frequency and weekdays together; reading a non-Custom draft as
        // "no weekdays" keeps every emitted state consistent, whatever order the writes arrive in.
        val parsed = Frequency.valueOf(frequency)
        Draft(name, unit, target, parsed, if (parsed == Frequency.CUSTOM) weekdays else Weekdays.NONE)
    }

    private val transientFlow: Flow<Transient> = combine(showErrors, duplicateName, failed, discardDialog, ::Transient)

    val uiState: StateFlow<HabitFormUiState> = combine(
        draftFlow,
        transientFlow,
        runner.isBusy(CommandRunner.CREATE_HABIT_KEY),
    ) { draft, transient, saving -> buildState(draft, transient, saving) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            buildState(currentDraft(), Transient(restoredFromSavedState, null, false, false), saving = false),
        )

    fun onNameChange(value: String) {
        savedState[KEY_NAME] = value
        duplicateName.value = null
        failed.value = false
    }

    fun onUnitChange(value: String) {
        savedState[KEY_UNIT] = value
        failed.value = false
    }

    fun onTargetChange(value: String) {
        savedState[KEY_TARGET] = value
        failed.value = false
    }

    fun onFrequencyChange(frequency: Frequency) {
        savedState[KEY_FREQUENCY] = frequency.name
        if (frequency != Frequency.CUSTOM) savedState[KEY_WEEKDAYS] = Weekdays.NONE
        failed.value = false
    }

    fun onWeekdayToggle(day: DayOfWeek) {
        savedState[KEY_WEEKDAYS] = currentDraft().weekdays xor Weekdays.bit(day)
        failed.value = false
    }

    fun onSave() {
        val draft = currentDraft()
        val raw = draft.toRaw()
        val locale = Locale.getDefault()
        if (validator.validateHabit(raw, locale) is HabitInputResult.Invalid) {
            showErrors.value = true
            return
        }
        failed.value = false
        val submission = runner.submit(CommandRunner.CREATE_HABIT_KEY) { repository.createHabit(raw, locale) }
        if (submission !is Submission.Accepted) return // a duplicate tap while one is already in flight
        viewModelScope.launch {
            when (val result = submission.ticket.await()) {
                is CommandResult.Failure -> failed.value = true
                is CommandResult.Success -> when (result.value) {
                    is CreateHabitResult.Created ->
                        _effects.send(HabitFormEffect.Saved(permissionPolicy.consumeRequestAfterCreation()))
                    is CreateHabitResult.InvalidInput -> showErrors.value = true
                    CreateHabitResult.DuplicateName -> {
                        duplicateName.value = raw.name.trim()
                        showErrors.value = true
                    }
                }
            }
        }
    }

    /** Back, Up and Cancel: a changed draft asks first; an untouched one, or one already saving, leaves directly. */
    fun onBack() {
        val askFirst = currentDraft().isDirty() && !runner.isBusyNow(CommandRunner.CREATE_HABIT_KEY)
        if (askFirst) discardDialog.value = true else _effects.trySend(HabitFormEffect.NavigateBack)
    }

    fun onKeepEditing() {
        discardDialog.value = false
    }

    fun onDiscard() {
        discardDialog.value = false
        _effects.trySend(HabitFormEffect.NavigateBack)
    }

    private fun currentDraft(): Draft {
        val frequency = Frequency.valueOf(savedState.get<String>(KEY_FREQUENCY) ?: Frequency.DAILY.name)
        return Draft(
            name = savedState.get<String>(KEY_NAME).orEmpty(),
            unit = savedState.get<String>(KEY_UNIT).orEmpty(),
            target = savedState.get<String>(KEY_TARGET).orEmpty(),
            frequency = frequency,
            weekdays = if (frequency == Frequency.CUSTOM) savedState.get<Int>(KEY_WEEKDAYS) ?: Weekdays.NONE else Weekdays.NONE,
        )
    }

    private fun Draft.toRaw() = RawHabitInput(name, unit, target, frequency, weekdays)

    private fun Draft.isDirty() =
        name.isNotEmpty() || unit.isNotEmpty() || target.isNotEmpty() || frequency != Frequency.DAILY || weekdays != Weekdays.NONE

    private fun buildState(draft: Draft, transient: Transient, saving: Boolean): HabitFormUiState {
        val show = transient.showErrors
        val locale = Locale.getDefault()
        val nameError: NameFieldError? = when {
            !show -> null
            transient.duplicateName != null -> NameFieldError.Duplicate(transient.duplicateName)
            else -> when ((validator.validateName(draft.name) as? FieldResult.Invalid)?.error) {
                NameError.EMPTY -> NameFieldError.Empty
                NameError.TOO_LONG -> NameFieldError.TooLong
                null -> null
            }
        }
        return HabitFormUiState(
            name = draft.name,
            unit = draft.unit,
            target = draft.target,
            frequency = draft.frequency,
            weekdays = draft.weekdays,
            nameError = nameError,
            unitError = if (show) (validator.validateUnit(draft.unit) as? FieldResult.Invalid)?.error else null,
            targetError = if (show) (validator.validateTarget(draft.target, locale) as? FieldResult.Invalid)?.error else null,
            weekdaysError = if (show) (validator.validateWeekdays(draft.frequency, draft.weekdays) as? FieldResult.Invalid)?.error else null,
            startsOn = startPreview(draft),
            submission = when {
                saving -> SubmissionState.Saving
                transient.failed -> SubmissionState.Failed
                else -> SubmissionState.Idle
            },
            isDirty = draft.isDirty(),
            showDiscardDialog = transient.discardDialog,
        )
    }

    private fun startPreview(draft: Draft): StartPreview? {
        if (draft.frequency == Frequency.CUSTOM && !Weekdays.isValidCustomMask(draft.weekdays)) return null
        val today = clock.snapshot().localDate
        val first = planner.firstStartDate(HabitSchedule(draft.frequency, draft.weekdays, today))
        return if (first == today) StartPreview.Today else StartPreview.On(first)
    }

    companion object {
        const val KEY_NAME = "form.name"
        const val KEY_UNIT = "form.unit"
        const val KEY_TARGET = "form.target"
        const val KEY_FREQUENCY = "form.frequency"
        const val KEY_WEEKDAYS = "form.weekdays"
        private val DRAFT_KEYS = listOf(KEY_NAME, KEY_UNIT, KEY_TARGET, KEY_FREQUENCY, KEY_WEEKDAYS)
        private const val STOP_TIMEOUT_MS = 5_000L
    }
}
