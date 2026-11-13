package com.codetutor.samplehabittrackerapp.data.local

import com.codetutor.samplehabittrackerapp.data.local.entity.HabitEntity
import com.codetutor.samplehabittrackerapp.data.local.entity.OccurrenceEntity
import com.codetutor.samplehabittrackerapp.domain.model.Habit
import com.codetutor.samplehabittrackerapp.domain.model.Occurrence

fun HabitEntity.toDomain() = Habit(
    habitId = habitId,
    name = name,
    nameKey = nameKey,
    unit = unit,
    frequency = frequency,
    selectedWeekdays = selectedWeekdays,
    creationLocalDate = creationLocalDate,
    currentTarget = currentTarget,
    status = status,
    plannedThroughLocalDate = plannedThroughLocalDate,
    retirementDeadlineEpochMillis = retirementDeadlineEpochMillis,
    retiredAtEpochMillis = retiredAtEpochMillis,
)

fun OccurrenceEntity.toDomain() = Occurrence(
    occurrenceId = occurrenceId,
    habitId = habitId,
    startLocalDate = startLocalDate,
    endLocalDate = endLocalDate,
    boundaryZoneId = boundaryZoneId,
    startInclusiveEpochMillis = startInclusiveEpochMillis,
    endExclusiveEpochMillis = endExclusiveEpochMillis,
    effectiveTarget = effectiveTarget,
    progress = progress,
    status = status,
    pointsEffect = pointsEffect,
    result = result,
    resolvedAtEpochMillis = resolvedAtEpochMillis,
    missAcknowledgedAtEpochMillis = missAcknowledgedAtEpochMillis,
)
