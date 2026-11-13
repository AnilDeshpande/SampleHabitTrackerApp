package com.codetutor.samplehabittrackerapp.feature.habitform

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.rules.DecimalError
import com.codetutor.samplehabittrackerapp.domain.rules.UnitError
import com.codetutor.samplehabittrackerapp.domain.rules.WeekdaysError
import java.time.LocalDate

/** The name field also reports a duplicate, which only the command's transaction can decide. */
sealed interface NameFieldError {
    data object Empty : NameFieldError
    data object TooLong : NameFieldError
    data class Duplicate(val name: String) : NameFieldError
}

/** Where the first period starts, for the "Starts ..." line. */
sealed interface StartPreview {
    data object Today : StartPreview
    data class On(val date: LocalDate) : StartPreview
}

enum class SubmissionState {
    Idle,

    /** The create command is in flight in the application scope (observed from the command runner). */
    Saving,

    /** The command failed and nothing was saved; every field is retained. */
    Failed,
}

/**
 * Raw text is restorable input and is held exactly as typed (never parsed here). Errors, the start
 * preview and control availability are derived from it, the clock and the locale on every change.
 */
data class HabitFormUiState(
    val name: String = "",
    val unit: String = "",
    val target: String = "",
    val frequency: Frequency = Frequency.DAILY,
    val weekdays: Int = 0,
    val nameError: NameFieldError? = null,
    val unitError: UnitError? = null,
    val targetError: DecimalError? = null,
    val weekdaysError: WeekdaysError? = null,
    val startsOn: StartPreview? = null,
    val submission: SubmissionState = SubmissionState.Idle,
    val isDirty: Boolean = false,
    val showDiscardDialog: Boolean = false,
) {
    val isSaving: Boolean get() = submission == SubmissionState.Saving
}

/** One-off events: never restored and never replayed. */
sealed interface HabitFormEffect {
    /** The habit committed. [requestNotificationPermission] is true at most once per install, on API 33+. */
    data class Saved(val requestNotificationPermission: Boolean) : HabitFormEffect

    data object NavigateBack : HabitFormEffect
}
