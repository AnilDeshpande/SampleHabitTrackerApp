package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot
import com.codetutor.samplehabittrackerapp.domain.model.CurrentStatus
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Habit
import com.codetutor.samplehabittrackerapp.domain.model.HabitStatus
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.rules.ContinuityCalculator
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.OverviewAssembler
import java.math.BigDecimal
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OverviewAssemblerTest {
    private val assembler = OverviewAssembler(OccurrencePlanner(), ContinuityCalculator())
    private val utc = ZoneId.of("UTC")

    private fun snapshot(iso: String) = TimeSnapshot(Instant.parse(iso), utc)

    private fun habit(
        id: Long = 1,
        frequency: Frequency = Frequency.DAILY,
        weekdays: Int = 0,
        status: HabitStatus = HabitStatus.ACTIVE,
        watermark: String? = null,
        deadline: Long? = null,
        created: String = "2026-10-11",
    ) = Habit(
        habitId = id, name = "Habit $id", nameKey = "habit $id", unit = "minutes", frequency = frequency,
        selectedWeekdays = weekdays, creationLocalDate = LocalDate.parse(created), currentTarget = BigDecimal.TEN,
        status = status, plannedThroughLocalDate = watermark?.let(LocalDate::parse),
        retirementDeadlineEpochMillis = deadline, retiredAtEpochMillis = null,
    )

    private fun assemble(habits: List<Habit>, rows: List<com.codetutor.samplehabittrackerapp.domain.model.Occurrence>, now: String, badges: Set<Long> = emptySet(), points: Long = 0) =
        assembler.assemble(points, habits, rows, badges, snapshot(now))

    @Test fun noHabitsIsTheEmptyState() {
        val data = assemble(emptyList(), emptyList(), "2026-10-11T10:00:00Z")
        assertTrue(data.isEmpty)
    }

    @Test fun openStartedPeriodIsInProgress() {
        val today = dailyOccurrence(1, LocalDate.parse("2026-10-11"), null)
        val data = assemble(listOf(habit(watermark = "2026-10-11")), listOf(today), "2026-10-11T10:00:00Z")
        val status = data.habits.single().status as CurrentStatus.InProgress
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), status.endsAt)
        assertEquals(1, data.habits.single().activeRun)
    }

    @Test fun noRowsMeansWaitingForTheFirstPeriod() {
        val weekly = habit(frequency = Frequency.WEEKLY)
        val data = assemble(listOf(weekly), emptyList(), "2026-10-11T10:00:00Z")
        assertEquals(
            CurrentStatus.WaitingForFirstPeriod(Instant.parse("2026-10-12T00:00:00Z")),
            data.habits.single().status,
        )
        assertEquals(0, data.habits.single().activeRun)
    }

    @Test fun customHabitOnAnUnselectedDayHasNoOccurrenceToday() {
        val mwf = Weekdays.maskOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val monday = dailyOccurrence(1, LocalDate.parse("2026-10-12"), OccurrenceResult.SUCCESS)
        val custom = habit(frequency = Frequency.CUSTOM, weekdays = mwf, watermark = "2026-10-12")
        val data = assemble(listOf(custom), listOf(monday), "2026-10-13T10:00:00Z") // Tuesday
        assertEquals(
            CurrentStatus.NoOccurrenceToday(Instant.parse("2026-10-14T00:00:00Z")),
            data.habits.single().status,
        )
    }

    @Test fun aGapBetweenDailyPeriodsIsBetweenPeriods() {
        // The previous period ended 22:00Z; the next local day (in UTC) starts 00:00Z. At 23:00Z nothing is open.
        val row = dailyOccurrence(1, LocalDate.parse("2026-10-11"), OccurrenceResult.MISS)
            .copy(endExclusiveEpochMillis = ms("2026-10-11T22:00:00Z"))
        val data = assemble(listOf(habit(watermark = "2026-10-11")), listOf(row), "2026-10-11T23:00:00Z")
        assertEquals(
            CurrentStatus.BetweenPeriods(Instant.parse("2026-10-12T00:00:00Z")),
            data.habits.single().status,
        )
    }

    @Test fun derivesContinuityBadgeMissesAndLastResult() {
        val rows = listOf(
            dailyOccurrence(1, LocalDate.parse("2026-10-09"), OccurrenceResult.SUCCESS).copy(pointsEffect = 10),
            dailyOccurrence(2, LocalDate.parse("2026-10-10"), OccurrenceResult.MISS).copy(pointsEffect = -1),
            dailyOccurrence(3, LocalDate.parse("2026-10-11"), null),
        )
        val data = assemble(listOf(habit(watermark = "2026-10-11")), rows, "2026-10-11T10:00:00Z", badges = setOf(1L), points = 9)
        val overview = data.habits.single()
        assertEquals(9L, data.points)
        assertEquals(3, overview.activeRun)
        assertEquals(0, overview.successStreak)
        assertTrue(overview.badgeEarned)
        assertEquals(1, overview.unacknowledgedMisses)
        assertEquals(OccurrenceResult.MISS, overview.lastResult!!.result)
        assertEquals(-1L, overview.lastResult!!.pointsEffect)
    }

    @Test fun acknowledgedMissesAreNotCounted() {
        val acknowledged = dailyOccurrence(1, LocalDate.parse("2026-10-10"), OccurrenceResult.MISS)
            .copy(missAcknowledgedAtEpochMillis = 5)
        val open = dailyOccurrence(2, LocalDate.parse("2026-10-11"), null)
        val data = assemble(listOf(habit(watermark = "2026-10-11")), listOf(acknowledged, open), "2026-10-11T10:00:00Z")
        assertEquals(0, data.habits.single().unacknowledgedMisses)
    }

    @Test fun pendingHabitsLeaveTheActiveListAndShowTheirDeadline() {
        val pending = habit(id = 2, status = HabitStatus.PENDING_RETIREMENT, deadline = 99_000)
        val data = assemble(listOf(habit(id = 1), pending), emptyList(), "2026-10-11T10:00:00Z")
        assertEquals(listOf(1L), data.habits.map { it.habitId })
        assertEquals(listOf(2L), data.pendingRetirements.map { it.habitId })
        assertEquals(99_000L, data.pendingRetirements.single().deadlineEpochMillis)
        assertFalse(data.isEmpty)
    }
}
