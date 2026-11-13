package com.codetutor.samplehabittrackerapp.feature.overview

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import java.time.Instant

/**
 * A read projection of durable data, never a second writable store. The overview has no unsaved
 * business input, so there is nothing to restore: everything is reloaded or derived.
 */
sealed interface OverviewUiState {
    /** Before the first read completes. The screen shows a wait indicator only after 300 ms (S88). */
    data object Loading : OverviewUiState

    /** The local read failed. Never rendered as an empty state, and it blocks actions that need the data (S89). */
    data object ReadError : OverviewUiState

    data class Content(
        val points: Long,
        val habits: List<HabitCardUi>,
        val pendingRetirements: List<PendingRetirementUi>,
    ) : OverviewUiState {
        /** The no-habits state (S1, S116). */
        val isEmpty: Boolean get() = habits.isEmpty() && pendingRetirements.isEmpty()
    }
}

data class HabitCardUi(
    val habitId: Long,
    val name: String,
    val unit: String,
    val frequency: Frequency,
    val selectedWeekdays: Int,
    val targetText: String,
    val status: HabitStatusUi,
    val activeRun: Int,
    val successStreak: Int,
    val badgeEarned: Boolean,
    val unacknowledgedMisses: Int,
    val lastResult: LastResultUi?,
) {
    /** Record progress is offered only while a period is open. */
    val canRecordProgress: Boolean get() = status is HabitStatusUi.InProgress
}

sealed interface HabitStatusUi {
    data class InProgress(
        val progressText: String,
        val targetText: String,
        val targetMet: Boolean,
        val endsAt: Instant,
    ) : HabitStatusUi

    data class WaitingForFirstPeriod(val startsAt: Instant) : HabitStatusUi
    data class NoOccurrenceToday(val nextStartsAt: Instant) : HabitStatusUi
    data class BetweenPeriods(val nextStartsAt: Instant) : HabitStatusUi
}

/** [pointsMagnitude] is unsigned; the sign comes from [result] so the text never relies on colour. */
data class LastResultUi(val result: OccurrenceResult, val pointsMagnitude: Long)

data class PendingRetirementUi(val habitId: Long, val name: String)
