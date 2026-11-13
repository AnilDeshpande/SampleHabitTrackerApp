package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/** The fixed part of a habit that decides its periods. */
data class HabitSchedule(
    val frequency: Frequency,
    val selectedWeekdays: Int,
    val creationLocalDate: LocalDate,
) {
    init {
        if (frequency == Frequency.CUSTOM) {
            require(Weekdays.isValidCustomMask(selectedWeekdays)) { "Custom needs weekdays 1..127" }
        } else {
            require(selectedWeekdays == Weekdays.NONE) { "Only Custom stores weekdays" }
        }
    }
}

/**
 * What the planner already decided for a habit.
 *
 * @property plannedThroughLocalDate the watermark: every candidate starting on or before it was
 * created or skipped; null before the first decision.
 * @property previousEndExclusiveEpochMillis end of the habit's latest occurrence, if any.
 */
data class PlanningState(
    val plannedThroughLocalDate: LocalDate?,
    val previousEndExclusiveEpochMillis: Long?,
)

data class PlannedOccurrence(
    val startLocalDate: LocalDate,
    val endLocalDate: LocalDate,
    val boundaryZoneId: String,
    val startInclusiveEpochMillis: Long,
    val endExclusiveEpochMillis: Long,
)

data class PlanResult(
    val occurrences: List<PlannedOccurrence>,
    /** The new watermark to store; unchanged when nothing was decided. */
    val plannedThroughLocalDate: LocalDate?,
)

data class NextOccurrence(val startLocalDate: LocalDate, val startInstant: Instant)

/**
 * Pure period planner (ADR 0002). A row is created only once its period starts, never starts
 * before the previous row ends, and a candidate left with no time is skipped. Stored boundaries
 * are never recomputed: only candidates after the watermark use the snapshot's current zone.
 */
class OccurrencePlanner {

    /** The local date of a habit's first period, which the create form previews. */
    fun firstStartDate(schedule: HabitSchedule): LocalDate =
        candidates(schedule, after = null).first().start

    fun plan(schedule: HabitSchedule, state: PlanningState, snapshot: TimeSnapshot): PlanResult {
        val zone = snapshot.zone
        val now = snapshot.epochMillis
        val created = mutableListOf<PlannedOccurrence>()
        var previousEnd = state.previousEndExclusiveEpochMillis
        var watermark = state.plannedThroughLocalDate

        for (candidate in candidates(schedule, state.plannedThroughLocalDate)) {
            val bounds = boundsOf(candidate, previousEnd, zone)
            if (bounds.start >= bounds.end) {
                watermark = candidate.start
                continue
            }
            if (bounds.start > now) break
            created += PlannedOccurrence(
                startLocalDate = candidate.start,
                endLocalDate = candidate.end,
                boundaryZoneId = zone.id,
                startInclusiveEpochMillis = bounds.start,
                endExclusiveEpochMillis = bounds.end,
            )
            previousEnd = bounds.end
            watermark = candidate.start
        }
        return PlanResult(created, watermark)
    }

    /**
     * The next candidate that is not yet created and would not be skipped, or null if none exists.
     * Pass the state as it is after any [plan] result was applied.
     */
    fun nextOccurrence(schedule: HabitSchedule, state: PlanningState, zone: ZoneId): NextOccurrence? {
        val previousEnd = state.previousEndExclusiveEpochMillis
        for (candidate in candidates(schedule, state.plannedThroughLocalDate)) {
            val bounds = boundsOf(candidate, previousEnd, zone)
            if (bounds.start < bounds.end) {
                return NextOccurrence(candidate.start, Instant.ofEpochMilli(bounds.start))
            }
        }
        return null
    }

    private data class Candidate(val start: LocalDate, val end: LocalDate)

    private data class Bounds(val start: Long, val end: Long)

    private fun boundsOf(candidate: Candidate, previousEnd: Long?, zone: ZoneId): Bounds {
        val localStart = candidate.start.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = candidate.end.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val start = if (previousEnd != null) maxOf(localStart, previousEnd) else localStart
        return Bounds(start, end)
    }

    private fun candidates(schedule: HabitSchedule, after: LocalDate?): Sequence<Candidate> {
        val all: Sequence<Candidate> = when (schedule.frequency) {
            Frequency.DAILY ->
                generateSequence(schedule.creationLocalDate) { it.plusDays(1) }
                    .map { Candidate(it, it) }

            Frequency.WEEKLY ->
                generateSequence(schedule.creationLocalDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY))) {
                    it.plusWeeks(1)
                }.map { Candidate(it, it.plusDays(6)) }

            Frequency.FORTNIGHTLY ->
                generateSequence(schedule.creationLocalDate) { it.plusDays(14) }
                    .map { Candidate(it, it.plusDays(13)) }

            Frequency.MONTHLY -> {
                val creation = schedule.creationLocalDate
                val first = if (creation.dayOfMonth == 1) creation else creation.withDayOfMonth(1).plusMonths(1)
                generateSequence(first) { it.plusMonths(1) }
                    .map { Candidate(it, it.with(TemporalAdjusters.lastDayOfMonth())) }
            }

            Frequency.CUSTOM ->
                generateSequence(schedule.creationLocalDate) { it.plusDays(1) }
                    .filter { Weekdays.contains(schedule.selectedWeekdays, it.dayOfWeek) }
                    .map { Candidate(it, it) }
        }
        return if (after == null) all else all.dropWhile { !it.start.isAfter(after) }
    }
}
