package com.codetutor.samplehabittrackerapp

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codetutor.samplehabittrackerapp.feature.overview.HabitSavedFeedback
import com.codetutor.samplehabittrackerapp.navigation.OverviewResult
import com.codetutor.samplehabittrackerapp.ui.theme.SampleHabitTrackerAppTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The "Habit saved" confirmation after Create habit returns: on the ordinary path, after the permission dialog
 * on the prompt path, and once only. The system dialog itself is replaced by a recorded callback.
 */
@RunWith(AndroidJUnit4::class)
class HabitSavedFeedbackTest {
    @get:Rule val compose = createComposeRule()

    private val results = SavedStateHandle()
    private var dialogsOpened = 0
    private var dialogClosed: (() -> Unit)? = null

    private fun show() {
        compose.setContent {
            SampleHabitTrackerAppTheme {
                val snackbar = androidx.compose.runtime.remember { SnackbarHostState() }
                Box { SnackbarHost(snackbar) }
                HabitSavedFeedback(results, snackbar) { onClosed -> dialogsOpened++; dialogClosed = onClosed }
            }
        }
    }

    private fun returnFromCreate(askPermission: Boolean) = compose.runOnIdle {
        results[OverviewResult.KEY_REQUEST_NOTIFICATION_PERMISSION] = askPermission
        results[OverviewResult.KEY_HABIT_SAVED] = true
    }

    @Test fun theConfirmationStaysVisibleOnTheNoDialogPath() {
        show()
        returnFromCreate(askPermission = false)
        compose.onNodeWithText("Habit saved").assertIsDisplayed()
        assertEquals("no permission dialog on this path", 0, dialogsOpened)
        // The result was consumed (so it is not replayed) while the message remained on screen.
        compose.runOnIdle {
            assertEquals(false, results.get<Boolean>(OverviewResult.KEY_HABIT_SAVED))
            assertEquals(false, results.get<Boolean>(OverviewResult.KEY_REQUEST_NOTIFICATION_PERMISSION))
        }
        compose.onNodeWithText("Habit saved").assertIsDisplayed()
    }

    @Test fun theConfirmationAppearsAfterThePermissionDialogCloses() {
        show()
        returnFromCreate(askPermission = true)
        compose.waitUntil(5_000) { dialogsOpened == 1 }
        compose.onNodeWithText("Habit saved").assertDoesNotExistYet()
        compose.runOnIdle { dialogClosed!!.invoke() }
        compose.onNodeWithText("Habit saved").assertIsDisplayed()
        compose.runOnIdle { assertEquals(false, results.get<Boolean>(OverviewResult.KEY_REQUEST_NOTIFICATION_PERMISSION)) }
    }

    @Test fun aResultIsShownOnceAndASecondSaveShowsItAgain() {
        show()
        returnFromCreate(askPermission = false)
        compose.onNodeWithText("Habit saved").assertIsDisplayed()
        compose.waitUntil(10_000) { compose.onAllNodesWithTextCount("Habit saved") == 0 } // the snackbar times out
        compose.mainClock.advanceTimeBy(1_000)
        assertEquals("not replayed", 0, compose.onAllNodesWithTextCount("Habit saved"))
        assertFalse(dialogsOpened > 0)

        returnFromCreate(askPermission = false)
        compose.onNodeWithText("Habit saved").assertIsDisplayed()
    }

    private fun androidx.compose.ui.test.SemanticsNodeInteraction.assertDoesNotExistYet() = assertDoesNotExist()

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.onAllNodesWithTextCount(text: String): Int =
        onAllNodes(androidx.compose.ui.test.hasText(text)).fetchSemanticsNodes().size
}
