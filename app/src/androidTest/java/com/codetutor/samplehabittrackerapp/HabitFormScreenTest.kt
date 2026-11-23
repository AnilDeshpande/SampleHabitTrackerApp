package com.codetutor.samplehabittrackerapp

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.rules.DecimalError
import com.codetutor.samplehabittrackerapp.domain.rules.UnitError
import com.codetutor.samplehabittrackerapp.domain.rules.WeekdaysError
import com.codetutor.samplehabittrackerapp.feature.habitform.HabitFormScreen
import com.codetutor.samplehabittrackerapp.feature.habitform.HabitFormUiState
import com.codetutor.samplehabittrackerapp.feature.habitform.NameFieldError
import com.codetutor.samplehabittrackerapp.feature.habitform.StartPreview
import com.codetutor.samplehabittrackerapp.feature.habitform.SubmissionState
import com.codetutor.samplehabittrackerapp.ui.theme.SampleHabitTrackerAppTheme
import com.codetutor.samplehabittrackerapp.ui.time.TimeDisplay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Renders the stateless create form for each state; the ViewModel's behavior is tested on the host. */
@RunWith(AndroidJUnit4::class)
class HabitFormScreenTest {
    @get:Rule val compose = createComposeRule()

    private val names = mutableListOf<String>()
    private val units = mutableListOf<String>()
    private val targets = mutableListOf<String>()
    private val frequencies = mutableListOf<Frequency>()
    private val days = mutableListOf<DayOfWeek>()
    private var saves = 0
    private var backs = 0
    private var keeps = 0
    private var discards = 0

    private fun show(state: HabitFormUiState, timeDisplay: TimeDisplay = TimeDisplay(ZoneId.of("UTC"), is24Hour = true)) {
        compose.setContent {
            SampleHabitTrackerAppTheme {
                HabitFormScreen(
                    timeDisplay = timeDisplay,
                    state = state,
                    onNameChange = { names += it },
                    onUnitChange = { units += it },
                    onTargetChange = { targets += it },
                    onFrequencyChange = { frequencies += it },
                    onWeekdayToggle = { days += it },
                    onSave = { saves++ },
                    onBack = { backs++ },
                    onKeepEditing = { keeps++ },
                    onDiscard = { discards++ },
                )
            }
        }
    }

    private fun day(day: DayOfWeek) = day.getDisplayName(TextStyle.SHORT, Locale.getDefault())

    @Test fun s2ShowsEveryFieldHelperAndAction() {
        show(HabitFormUiState(startsOn = StartPreview.Today()))
        compose.onNodeWithText("Create habit").assertIsDisplayed()
        compose.onNodeWithText("1–50 characters; unique active name").assertIsDisplayed()
        compose.onNodeWithText("1–20 characters; fixed after save").assertIsDisplayed()
        compose.onNodeWithText("Positive; up to 9 + 3 digits").assertIsDisplayed()
        compose.onNodeWithText("Daily").assertIsSelected()
        compose.onNodeWithText("Starts today").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Save habit").performScrollTo().assertIsEnabled()
        compose.onNodeWithText("Cancel").performScrollTo().assertIsDisplayed()
    }

    @Test fun typingAndChoosingReportEachEvent() {
        show(HabitFormUiState())
        compose.onNodeWithText("Name").performTextInput("Meditation")
        compose.onNodeWithText("Unit").performTextInput("minutes")
        compose.onNodeWithText("Target").performTextInput("10")
        compose.onNodeWithText("Weekly").performScrollTo().performClick()
        compose.onNodeWithText("Save habit").performScrollTo().performClick()
        assertEquals(listOf("Meditation"), names)
        assertEquals(listOf("minutes"), units)
        assertEquals(listOf("10"), targets)
        assertEquals(listOf(Frequency.WEEKLY), frequencies)
        assertEquals(1, saves)
    }

    @Test fun s21ShowsFieldSpecificErrorsAndKeepsTheValues() {
        show(
            HabitFormUiState(
                name = "Meditation", unit = "", target = "1e3",
                nameError = NameFieldError.Duplicate("Meditation"),
                unitError = UnitError.EMPTY,
                targetError = DecimalError.NOT_A_NUMBER,
                startsOn = StartPreview.Today(),
            ),
        )
        compose.onNodeWithText("An active Meditation already exists").assertIsDisplayed()
        compose.onNodeWithText("Enter a unit").assertIsDisplayed()
        compose.onNodeWithText("Enter a positive number without a sign").assertIsDisplayed()
        compose.onNodeWithText("Meditation").assertIsDisplayed()
        compose.onNodeWithText("1e3").assertIsDisplayed()
    }

    @Test fun startsOnADateForWeekly() {
        show(HabitFormUiState(frequency = Frequency.WEEKLY, startsOn = StartPreview.On(LocalDate.parse("2026-10-12"))))
        compose.onNodeWithText("Weekly").assertIsSelected()
        compose.onNodeWithText("Starts Mon 12 Oct", substring = true).performScrollTo().assertIsDisplayed()
    }

    @Test fun weeklyPreviewShowsBothEndsOfTheFirstPeriod() {
        show(HabitFormUiState(frequency = Frequency.WEEKLY, startsOn = StartPreview.On(LocalDate.parse("2026-10-12"), endsOn = LocalDate.parse("2026-10-19"))))
        compose.onNodeWithText("Starts Mon 12 Oct · Ends Mon 19 Oct 00:00").performScrollTo().assertIsDisplayed()
    }

    @Test fun fortnightlyPreviewShowsBothEndsOfTheFirstPeriod() {
        show(HabitFormUiState(frequency = Frequency.FORTNIGHTLY, startsOn = StartPreview.Today(endsOn = LocalDate.parse("2026-10-25"))))
        compose.onNodeWithText("Starts today · Ends Sun 25 Oct 00:00").performScrollTo().assertIsDisplayed()
    }

    @Test fun monthlyPreviewShowsBothEndsOfTheFirstPeriod() {
        show(HabitFormUiState(frequency = Frequency.MONTHLY, startsOn = StartPreview.On(LocalDate.parse("2026-11-01"), endsOn = LocalDate.parse("2026-12-01"))))
        compose.onNodeWithText("Starts Sun 1 Nov · Ends Tue 1 Dec 00:00").performScrollTo().assertIsDisplayed()
    }

    @Test fun thePreviewEndFollowsTheTwelveHourSetting() {
        show(
            HabitFormUiState(frequency = Frequency.WEEKLY, startsOn = StartPreview.On(LocalDate.parse("2026-10-12"), endsOn = LocalDate.parse("2026-10-19"))),
            TimeDisplay(ZoneId.of("UTC"), is24Hour = false),
        )
        compose.onNode(hasText("Ends Mon 19 Oct 12:00", substring = true) and hasText("AM", substring = true)).performScrollTo().assertIsDisplayed()
    }

    @Test fun customShowsSevenWeekdayChipsAndReportsTheToggledDay() {
        show(HabitFormUiState(frequency = Frequency.CUSTOM, weekdays = 21, weekdaysError = null, startsOn = StartPreview.On(LocalDate.parse("2026-10-12"))))
        DayOfWeek.entries.forEach { compose.onNodeWithText(day(it)).performScrollTo().assertIsDisplayed() }
        compose.onNodeWithText(day(DayOfWeek.MONDAY)).assertIsSelected()
        compose.onNodeWithText(day(DayOfWeek.TUESDAY)).performScrollTo().performClick()
        assertEquals(listOf(DayOfWeek.TUESDAY), days)
    }

    @Test fun customWithNoDayShowsTheWeekdayError() {
        show(HabitFormUiState(frequency = Frequency.CUSTOM, weekdays = 0, weekdaysError = WeekdaysError.NONE_SELECTED))
        compose.onNodeWithText("Select at least one day").performScrollTo().assertIsDisplayed()
    }

    @Test fun savingDisablesOnlyTheSaveButton() {
        show(HabitFormUiState(name = "Meditation", unit = "minutes", target = "10", submission = SubmissionState.Saving))
        compose.onNodeWithText("Saving…").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("Cancel").performScrollTo().assertIsEnabled()
    }

    @Test fun aFailedSaveKeepsTheInputAndOffersRetry() {
        show(HabitFormUiState(name = "Meditation", unit = "minutes", target = "10", submission = SubmissionState.Failed))
        compose.onNodeWithText("Your input is retained. Nothing was saved.").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Retry").performScrollTo().assertIsEnabled().performClick()
        assertEquals(1, saves)
        compose.onNodeWithText("Meditation").performScrollTo().assertIsDisplayed()
    }

    @Test fun backAndCancelBothAskTheViewModel() {
        show(HabitFormUiState())
        compose.onNodeWithContentDescription("Back").performClick()
        compose.onNodeWithText("Cancel").performScrollTo().performClick()
        assertEquals(2, backs)
    }

    @Test fun s44TheDiscardDialogOffersKeepEditingAndDiscard() {
        show(HabitFormUiState(name = "Med", isDirty = true, showDiscardDialog = true))
        compose.onNodeWithText("Discard changes?").assertIsDisplayed()
        compose.onNodeWithText("Your unsaved changes will be lost.").assertIsDisplayed()
        compose.onNodeWithText("Keep editing").performClick()
        compose.onNodeWithText("Discard").performClick()
        assertEquals(1, keeps)
        assertEquals(1, discards)
    }
}
