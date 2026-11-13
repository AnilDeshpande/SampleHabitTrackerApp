package com.codetutor.samplehabittrackerapp

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.feature.overview.HabitCardUi
import com.codetutor.samplehabittrackerapp.feature.overview.HabitStatusUi
import com.codetutor.samplehabittrackerapp.feature.overview.LastResultUi
import com.codetutor.samplehabittrackerapp.feature.overview.OverviewScreen
import com.codetutor.samplehabittrackerapp.feature.overview.OverviewUiState
import com.codetutor.samplehabittrackerapp.ui.theme.SampleHabitTrackerAppTheme
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Renders the stateless overview for each state. Behavior (ViewModel, repository) is tested elsewhere. */
@RunWith(AndroidJUnit4::class)
class OverviewScreenTest {
    @get:Rule val compose = createComposeRule()

    private var created = 0
    private var retired = 0
    private var retries = 0
    private val recorded = mutableListOf<Long>()
    private val reviewed = mutableListOf<Long>()

    private fun show(state: OverviewUiState, fontScale: Float? = null) {
        compose.setContent {
            SampleHabitTrackerAppTheme {
                val screen = @androidx.compose.runtime.Composable {
                    OverviewScreen(
                        state = state,
                        snackbarHostState = SnackbarHostState(),
                        onCreateHabit = { created++ },
                        onRetiredHabits = { retired++ },
                        onRetry = { retries++ },
                        onRecordProgress = { recorded += it },
                        onReviewHabit = { reviewed += it },
                    )
                }
                if (fontScale == null) screen() else {
                    CompositionLocalProvider(LocalDensity provides Density(LocalDensity.current.density, fontScale)) { screen() }
                }
            }
        }
    }

    private fun card(
        status: HabitStatusUi,
        frequency: Frequency = Frequency.DAILY,
        lastResult: LastResultUi? = null,
        activeRun: Int = 1,
    ) = HabitCardUi(
        habitId = 7, name = "Meditation", unit = "minutes", frequency = frequency, selectedWeekdays = 0,
        targetText = "10", status = status, activeRun = activeRun, successStreak = 0, badgeEarned = false,
        unacknowledgedMisses = 0, lastResult = lastResult,
    )

    private val inProgress = HabitStatusUi.InProgress("0", "10", targetMet = false, endsAt = Instant.parse("2026-10-12T00:00:00Z"))

    @Test fun s1EmptyShowsThePromptAndBothActions() {
        show(OverviewUiState.Content(0, emptyList(), emptyList()))
        compose.onNodeWithText("Overview").assertIsDisplayed()
        compose.onNodeWithContentDescription("Global points: 0").assertIsDisplayed()
        compose.onNodeWithText("Start your first habit").assertIsDisplayed()
        compose.onNodeWithText("Create a habit with a measurable target.").assertIsDisplayed()
        compose.onNodeWithText("Create habit").performClick()
        compose.onNodeWithText("Retired habits").performClick()
        assertEquals(1, created)
        assertEquals(1, retired)
    }

    @Test fun loadingShowsNoIndicatorUntilThreeHundredMilliseconds() {
        compose.mainClock.autoAdvance = false
        show(OverviewUiState.Loading)
        compose.mainClock.advanceTimeBy(200)
        compose.onNodeWithText("Loading habits…").assertDoesNotExist()
        compose.mainClock.advanceTimeBy(300)
        compose.onNodeWithText("Loading habits…").assertIsDisplayed()
    }

    @Test fun aReadErrorOffersRetryAndNeverACreatePath() {
        show(OverviewUiState.ReadError)
        compose.onNodeWithText("Try again to load your saved habits.").assertIsDisplayed()
        compose.onNodeWithText("Create habit").assertDoesNotExist()
        compose.onNodeWithText("Start your first habit").assertDoesNotExist()
        compose.onNodeWithText("Try again").performClick()
        assertEquals(1, retries)
    }

    @Test fun s3DailyCardShowsEveryRequiredValue() {
        show(OverviewUiState.Content(0, listOf(card(inProgress)), emptyList()))
        compose.onNodeWithText("Meditation · Daily").assertIsDisplayed()
        compose.onNodeWithText("10 minutes target").assertIsDisplayed()
        compose.onNodeWithText("Today: 0 / 10 minutes").assertIsDisplayed()
        compose.onNodeWithText("Active run 1 · Streak 0").assertIsDisplayed()
        compose.onNodeWithText("Badge: not earned").assertIsDisplayed()
        compose.onNodeWithText("Unacknowledged misses: 0").assertIsDisplayed()
        compose.onNodeWithContentDescription("Record progress for Meditation").performClick()
        compose.onNodeWithContentDescription("Review Meditation").performClick()
        assertEquals(listOf(7L), recorded)
        assertEquals(listOf(7L), reviewed)
        compose.onNodeWithText("Create habit", useUnmergedTree = true).assertExists() // the floating action once habits exist
    }

    @Test fun aWaitingHabitStatesWhenItStartsAndOffersNoRecordProgress() {
        val waiting = HabitStatusUi.WaitingForFirstPeriod(Instant.parse("2026-10-12T00:00:00Z"))
        show(OverviewUiState.Content(0, listOf(card(waiting, Frequency.WEEKLY, activeRun = 0)), emptyList()))
        compose.onNodeWithText("Waiting for first period").assertIsDisplayed()
        compose.onNode(hasContentDescription("Record progress for Meditation")).assertDoesNotExist()
        compose.onNodeWithContentDescription("Review Meditation").assertIsDisplayed()
    }

    @Test fun targetMetIsStatedInWords() {
        val met = HabitStatusUi.InProgress("12", "10", targetMet = true, endsAt = Instant.parse("2026-10-12T00:00:00Z"))
        show(OverviewUiState.Content(0, listOf(card(met)), emptyList()))
        compose.onNodeWithText("Target met · period still open").assertIsDisplayed()
    }

    @Test fun lastResultsUseWordsAndSignsNotColour() {
        show(OverviewUiState.Content(10, listOf(card(inProgress, lastResult = LastResultUi(OccurrenceResult.SUCCESS, 10))), emptyList()))
        compose.onNodeWithText("Last result: Success · +10").assertIsDisplayed()
    }

    @Test fun aMissIsStatedWithItsDeduction() {
        show(OverviewUiState.Content(98, listOf(card(inProgress, lastResult = LastResultUi(OccurrenceResult.MISS, 1))), emptyList()))
        compose.onNodeWithText("Last result: Missed · −1").assertIsDisplayed()
    }

    @Test fun theEmptyStateStaysUsableAtTwiceTheFontSize() {
        show(OverviewUiState.Content(0, emptyList(), emptyList()), fontScale = 2f)
        compose.onNodeWithText("Start your first habit").assertIsDisplayed()
        compose.onNodeWithText("Create habit").assertIsDisplayed().performClick()
        assertEquals(1, created)
    }
}
