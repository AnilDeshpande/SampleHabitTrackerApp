package com.codetutor.samplehabittrackerapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.codetutor.samplehabittrackerapp.R
import com.codetutor.samplehabittrackerapp.feature.habitform.HabitFormRoute
import com.codetutor.samplehabittrackerapp.feature.overview.OverviewRoute
import com.codetutor.samplehabittrackerapp.feature.placeholder.PlaceholderScreen

/**
 * The whole navigation graph (LLD route register). Overview and Create habit are real screens; the
 * other destinations are wired now with placeholders so the flow is reachable end to end and each
 * later increment replaces one placeholder.
 */
@Composable
fun HabitTrackerNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    val habitIdArgument = listOf(navArgument(Routes.ARG_HABIT_ID) { type = NavType.LongType })
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.OVERVIEW, modifier = modifier) {
        composable(Routes.OVERVIEW) { entry ->
            OverviewRoute(
                backStackEntry = entry,
                onCreateHabit = { navController.navigate(Routes.CREATE_HABIT) },
                onRetiredHabits = { navController.navigate(Routes.RETIRED_HABITS) },
                onRecordProgress = { navController.navigate(Routes.recordProgress(it)) },
                onReviewHabit = { navController.navigate(Routes.habitReview(it)) },
            )
        }
        composable(Routes.CREATE_HABIT) {
            HabitFormRoute(
                onClose = back,
                onSaved = { requestNotificationPermission ->
                    // Hand one-shot results to the Overview entry that opened this form, then return to it.
                    navController.previousBackStackEntry?.savedStateHandle?.apply {
                        set(OverviewResult.KEY_HABIT_SAVED, true)
                        set(OverviewResult.KEY_REQUEST_NOTIFICATION_PERMISSION, requestNotificationPermission)
                    }
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.EDIT_HABIT, habitIdArgument) {
            PlaceholderScreen(stringResource(R.string.placeholder_edit_habit), back)
        }
        composable(Routes.RECORD_PROGRESS, habitIdArgument) {
            PlaceholderScreen(stringResource(R.string.placeholder_record_progress), back)
        }
        composable(Routes.HABIT_REVIEW, habitIdArgument) {
            PlaceholderScreen(stringResource(R.string.placeholder_habit_review), back)
        }
        composable(Routes.RETIREMENT_CONFIRM, habitIdArgument) {
            PlaceholderScreen(stringResource(R.string.placeholder_retirement), back)
        }
        composable(Routes.RETIRED_HABITS) {
            PlaceholderScreen(stringResource(R.string.placeholder_retired_habits), back)
        }
    }
}
