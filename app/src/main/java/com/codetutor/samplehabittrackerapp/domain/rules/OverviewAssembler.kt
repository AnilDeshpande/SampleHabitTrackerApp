package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot
import com.codetutor.samplehabittrackerapp.domain.model.CurrentStatus
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Habit
import com.codetutor.samplehabittrackerapp.domain.model.HabitOverview
import com.codetutor.samplehabittrackerapp.domain.model.HabitStatus
import com.codetutor.samplehabittrackerapp.domain.model.LastResult
import com.codetutor.samplehabittrackerapp.domain.model.Occurrence
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceStatus
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.model.PendingRetirement
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import java.time.Instant

/**
 * Builds the overview read model from persisted rows, with no database access. All derived values
 * live here: the current status, Active run, Success streak, badge flag and unacknowledged misses.
 */
class OverviewAssembler(
    private val planner: OccurrencePlanner,
    private val continuity: ContinuityCalculator,
) {
    fun assemble(
        pointsBalance: Long,
        habits: List<Habit>,
        occurrences: List<Occurrence>,
        habitIdsWithBadge: Set<Long>,
        snapshot: TimeSnapshot,
    ): OverviewData {
        val rowsByHabit = occurrences.groupBy { it.habitId }
        val active = habits
            .filter { it.status == HabitStatus.ACTIVE }
            .sortedBy { it.habitId }
            .map { habit ->
                val rows = rowsByHabit[habit.habitId].orEmpty()
                val run = continuity.calculate(rows)
                HabitOverview(
                    habitId = habit.habitId,
                    name = habit.name,
                    unit = habit.unit,
                    frequency = habit.frequency,
                    selectedWeekdays = habit.selectedWeekdays,
                    currentTarget = habit.currentTarget,
                    status = currentStatus(habit, rows, snapshot),
                    activeRun = run.activeRun,
                    successStreak = run.successStreak,
                    badgeEarned = habit.habitId in habitIdsWithBadge,
                    unacknowledgedMisses = rows.count {
                        it.result == OccurrenceResult.MISS && it.missAcknowledgedAtEpochMillis == null
                    },
                    lastResult = rows
                        .filter { it.status == OccurrenceStatus.FINALIZED }
                        .maxByOrNull { it.startInclusiveEpochMillis }
                        ?.let { LastResult(it.result!!, it.pointsEffect) },
                )
            }
        val pending = habits
            .filter { it.status == HabitStatus.PENDING_RETIREMENT }
            .sortedBy { it.habitId }
            .map { PendingRetirement(it.habitId, it.name, it.retirementDeadlineEpochMillis!!) }
        return OverviewData(pointsBalance, active, pending)
    }

    /**
     * An open period that has started is [CurrentStatus.InProgress]. Otherwise the habit is waiting
     * for its first period, has no occurrence today (a Custom habit on an unselected weekday), or
     * sits in a gap a timezone change left between periods.
     */
    private fun currentStatus(habit: Habit, rows: List<Occurrence>, snapshot: TimeSnapshot): CurrentStatus {
        val now = snapshot.epochMillis
        val open = rows.lastOrNull { it.status == OccurrenceStatus.OPEN }
        if (open != null && open.startInclusiveEpochMillis <= now) {
            return CurrentStatus.InProgress(
                target = open.effectiveTarget,
                progress = open.progress,
                endsAt = Instant.ofEpochMilli(open.endExclusiveEpochMillis),
            )
        }

        val next: Instant = open?.let { Instant.ofEpochMilli(it.startInclusiveEpochMillis) }
            ?: planner.nextOccurrence(
                HabitSchedule(habit.frequency, habit.selectedWeekdays, habit.creationLocalDate),
                PlanningState(habit.plannedThroughLocalDate, rows.maxOfOrNull { it.endExclusiveEpochMillis }),
                snapshot.zone,
            )?.startInstant
            ?: error("A habit schedule always has a next occurrence")

        return when {
            rows.none { it.status != OccurrenceStatus.CANCELLED } -> CurrentStatus.WaitingForFirstPeriod(next)
            habit.frequency == Frequency.CUSTOM &&
                !Weekdays.contains(habit.selectedWeekdays, snapshot.localDate.dayOfWeek) ->
                CurrentStatus.NoOccurrenceToday(next)
            else -> CurrentStatus.BetweenPeriods(next)
        }
    }
}
