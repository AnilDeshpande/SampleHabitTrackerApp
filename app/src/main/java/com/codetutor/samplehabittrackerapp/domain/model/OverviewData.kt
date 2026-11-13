package com.codetutor.samplehabittrackerapp.domain.model

import java.math.BigDecimal

/** The outcome of the habit's most recent finalized period. */
data class LastResult(val result: OccurrenceResult, val pointsEffect: Long)

/** One active habit as the overview shows it. Active run and Success streak are derived, never stored. */
data class HabitOverview(
    val habitId: Long,
    val name: String,
    val unit: String,
    val frequency: Frequency,
    val selectedWeekdays: Int,
    val currentTarget: BigDecimal,
    val status: CurrentStatus,
    val activeRun: Int,
    val successStreak: Int,
    val badgeEarned: Boolean,
    val unacknowledgedMisses: Int,
    val lastResult: LastResult?,
)

/** A habit whose retirement was confirmed and whose 10-second Undo window is still counting down. */
data class PendingRetirement(
    val habitId: Long,
    val name: String,
    val deadlineEpochMillis: Long,
)

data class OverviewData(
    val points: Long,
    val habits: List<HabitOverview>,
    val pendingRetirements: List<PendingRetirement>,
) {
    /** True when there is nothing to track and nothing pending: the no-habits state. */
    val isEmpty: Boolean get() = habits.isEmpty() && pendingRetirements.isEmpty()
}
