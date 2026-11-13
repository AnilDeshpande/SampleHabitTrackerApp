package com.codetutor.samplehabittrackerapp.feature.overview

import com.codetutor.samplehabittrackerapp.core.ui.DecimalFormatter
import com.codetutor.samplehabittrackerapp.domain.model.CurrentStatus
import com.codetutor.samplehabittrackerapp.domain.model.HabitOverview
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import java.util.Locale
import kotlin.math.abs

/** Maps the domain read model to the screen model. Formatting only: no rule is decided here. */
fun OverviewData.toUiState(formatter: DecimalFormatter, locale: Locale): OverviewUiState.Content =
    OverviewUiState.Content(
        points = points,
        habits = habits.map { it.toUi(formatter, locale) },
        pendingRetirements = pendingRetirements.map { PendingRetirementUi(it.habitId, it.name) },
    )

private fun HabitOverview.toUi(formatter: DecimalFormatter, locale: Locale) = HabitCardUi(
    habitId = habitId,
    name = name,
    unit = unit,
    frequency = frequency,
    selectedWeekdays = selectedWeekdays,
    targetText = formatter.format(currentTarget, locale),
    status = when (val s = status) {
        is CurrentStatus.InProgress -> HabitStatusUi.InProgress(
            progressText = formatter.format(s.progress, locale),
            targetText = formatter.format(s.target, locale),
            targetMet = s.progress.compareTo(s.target) >= 0,
            endsAt = s.endsAt,
        )
        is CurrentStatus.WaitingForFirstPeriod -> HabitStatusUi.WaitingForFirstPeriod(s.startsAt)
        is CurrentStatus.NoOccurrenceToday -> HabitStatusUi.NoOccurrenceToday(s.nextStartsAt)
        is CurrentStatus.BetweenPeriods -> HabitStatusUi.BetweenPeriods(s.nextStartsAt)
    },
    activeRun = activeRun,
    successStreak = successStreak,
    badgeEarned = badgeEarned,
    unacknowledgedMisses = unacknowledgedMisses,
    lastResult = lastResult?.let { LastResultUi(it.result, abs(it.pointsEffect)) },
)
