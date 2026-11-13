package com.codetutor.samplehabittrackerapp.navigation

/**
 * Route register (LLD). Habit arguments are positive Long ids; no route carries an entity, a draft or
 * an occurrence. Record progress is its own destination even though it may render as a sheet.
 */
object Routes {
    const val ARG_HABIT_ID = "habitId"

    const val OVERVIEW = "overview"
    const val CREATE_HABIT = "habit/create"
    const val EDIT_HABIT = "habit/edit/{$ARG_HABIT_ID}"
    const val RECORD_PROGRESS = "record-progress/{$ARG_HABIT_ID}"
    const val HABIT_REVIEW = "habit-review/{$ARG_HABIT_ID}"
    const val RETIREMENT_CONFIRM = "retirement-confirm/{$ARG_HABIT_ID}"
    const val RETIRED_HABITS = "retired-habits"

    fun editHabit(habitId: Long) = "habit/edit/$habitId"
    fun recordProgress(habitId: Long) = "record-progress/$habitId"
    fun habitReview(habitId: Long) = "habit-review/$habitId"
    fun retirementConfirm(habitId: Long) = "retirement-confirm/$habitId"
}

/** One-shot results the Create habit destination hands back to the Overview entry that opened it. */
object OverviewResult {
    const val KEY_HABIT_SAVED = "overview.habitSaved"
    const val KEY_REQUEST_NOTIFICATION_PERMISSION = "overview.requestNotificationPermission"
}
