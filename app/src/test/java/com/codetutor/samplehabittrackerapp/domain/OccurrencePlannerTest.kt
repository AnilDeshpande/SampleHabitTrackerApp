package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.rules.HabitSchedule
import com.codetutor.samplehabittrackerapp.domain.rules.OccurrencePlanner
import com.codetutor.samplehabittrackerapp.domain.rules.PlanningState
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class OccurrencePlannerTest {
    private val planner = OccurrencePlanner()
    private val utc: ZoneId = ZoneId.of("UTC")
    private val none = PlanningState(null, null)

    private fun date(s: String) = LocalDate.parse(s)
    private fun at(iso: String, zone: ZoneId = utc) = TimeSnapshot(Instant.parse(iso), zone)
    private fun schedule(frequency: Frequency, created: String, mask: Int = 0) =
        HabitSchedule(frequency, mask, date(created))

    // 2026-10-11 is a Sunday, 2026-10-12 a Monday.

    // ---- first period per cadence ----

    @Test fun dailyStartsOnTheCreationDate() {
        val s = schedule(Frequency.DAILY, "2026-10-11")
        assertEquals(date("2026-10-11"), planner.firstStartDate(s))
        val plan = planner.plan(s, none, at("2026-10-11T10:00:00Z"))
        assertEquals(1, plan.occurrences.size)
        val row = plan.occurrences.single()
        assertEquals(ms("2026-10-11T00:00:00Z"), row.startInclusiveEpochMillis)
        assertEquals(ms("2026-10-12T00:00:00Z"), row.endExclusiveEpochMillis)
        assertEquals(date("2026-10-11"), row.startLocalDate)
        assertEquals(date("2026-10-11"), row.endLocalDate)
        assertEquals("UTC", row.boundaryZoneId)
        assertEquals(date("2026-10-11"), plan.plannedThroughLocalDate)
    }

    @Test fun weeklyCreatedOnSundayWaitsForNextMonday() {
        val s = schedule(Frequency.WEEKLY, "2026-10-11")
        assertEquals(date("2026-10-12"), planner.firstStartDate(s))
        val plan = planner.plan(s, none, at("2026-10-11T10:00:00Z"))
        assertTrue(plan.occurrences.isEmpty())
        assertNull(plan.plannedThroughLocalDate)
        val next = planner.nextOccurrence(s, none, utc)!!
        assertEquals(date("2026-10-12"), next.startLocalDate)
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), next.startInstant)
    }

    @Test fun weeklyCreatedOnMondayStartsThatWeek() {
        val s = schedule(Frequency.WEEKLY, "2026-10-12")
        val plan = planner.plan(s, none, at("2026-10-12T09:00:00Z"))
        val row = plan.occurrences.single()
        assertEquals(date("2026-10-12"), row.startLocalDate)
        assertEquals(date("2026-10-18"), row.endLocalDate)
        assertEquals(ms("2026-10-19T00:00:00Z"), row.endExclusiveEpochMillis)
    }

    @Test fun weeklyPeriodOpensExactlyAtItsStartInstant() {
        val s = schedule(Frequency.WEEKLY, "2026-10-11")
        assertTrue(planner.plan(s, none, at("2026-10-11T23:59:59Z")).occurrences.isEmpty())
        assertEquals(1, planner.plan(s, none, at("2026-10-12T00:00:00Z")).occurrences.size)
    }

    @Test fun fortnightlyIsAnchoredToTheCreationDate() {
        val s = schedule(Frequency.FORTNIGHTLY, "2026-10-11")
        val plan = planner.plan(s, none, at("2026-10-11T10:00:00Z"))
        val row = plan.occurrences.single()
        assertEquals(date("2026-10-11"), row.startLocalDate)
        assertEquals(date("2026-10-24"), row.endLocalDate)
        assertEquals(ms("2026-10-25T00:00:00Z"), row.endExclusiveEpochMillis)
        val state = PlanningState(plan.plannedThroughLocalDate, row.endExclusiveEpochMillis)
        assertEquals(date("2026-10-25"), planner.nextOccurrence(s, state, utc)!!.startLocalDate)
    }

    @Test fun monthlyCreatedMidMonthWaitsForTheFirst() {
        val s = schedule(Frequency.MONTHLY, "2026-10-11")
        assertEquals(date("2026-11-01"), planner.firstStartDate(s))
        assertTrue(planner.plan(s, none, at("2026-10-11T10:00:00Z")).occurrences.isEmpty())
        val row = planner.plan(s, none, at("2026-11-01T00:00:00Z")).occurrences.single()
        assertEquals(date("2026-11-30"), row.endLocalDate)
        assertEquals(ms("2026-12-01T00:00:00Z"), row.endExclusiveEpochMillis)
    }

    @Test fun monthlyCreatedOnTheFirstStartsThatMonth() {
        val s = schedule(Frequency.MONTHLY, "2026-11-01")
        assertEquals(date("2026-11-01"), planner.firstStartDate(s))
    }

    @Test fun customStartsOnTheFirstSelectedDayOnOrAfterCreation() {
        val mwf = Weekdays.maskOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        assertEquals(date("2026-10-12"), planner.firstStartDate(schedule(Frequency.CUSTOM, "2026-10-11", mwf)))
        assertEquals(date("2026-10-12"), planner.firstStartDate(schedule(Frequency.CUSTOM, "2026-10-12", mwf)))
        assertEquals(date("2026-10-19"), planner.firstStartDate(schedule(Frequency.CUSTOM, "2026-10-17", mwf)))
    }

    @Test fun customHasNoOccurrenceOnUnselectedDays() {
        val mwf = Weekdays.maskOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val s = schedule(Frequency.CUSTOM, "2026-10-11", mwf)
        val plan = planner.plan(s, none, at("2026-10-18T12:00:00Z")) // Sunday
        assertEquals(
            listOf(date("2026-10-12"), date("2026-10-14"), date("2026-10-16")),
            plan.occurrences.map { it.startLocalDate },
        )
        assertEquals(date("2026-10-16"), plan.plannedThroughLocalDate)
        val state = PlanningState(plan.plannedThroughLocalDate, plan.occurrences.last().endExclusiveEpochMillis)
        assertEquals(date("2026-10-19"), planner.nextOccurrence(s, state, utc)!!.startLocalDate)
    }

    // ---- catch-up and watermark ----

    @Test fun catchUpCreatesEveryStartedPeriodWithoutOverlap() {
        val s = schedule(Frequency.DAILY, "2026-10-11")
        val plan = planner.plan(s, none, at("2026-11-10T08:00:00Z"))
        assertEquals(31, plan.occurrences.size)
        assertEquals(date("2026-11-10"), plan.plannedThroughLocalDate)
        plan.occurrences.zipWithNext().forEach { (a, b) ->
            assertEquals(a.endExclusiveEpochMillis, b.startInclusiveEpochMillis)
        }
    }

    @Test fun watermarkPreventsReplanningDecidedDates() {
        val s = schedule(Frequency.DAILY, "2026-10-11")
        val state = PlanningState(date("2026-10-12"), ms("2026-10-13T00:00:00Z"))
        val plan = planner.plan(s, state, at("2026-10-12T12:00:00Z"))
        assertTrue(plan.occurrences.isEmpty())
        assertEquals(date("2026-10-12"), plan.plannedThroughLocalDate)
    }

    @Test fun clockMovedBackNeverReopensOrRecreates() {
        val s = schedule(Frequency.DAILY, "2026-10-11")
        val state = PlanningState(date("2026-10-14"), ms("2026-10-15T00:00:00Z"))
        val plan = planner.plan(s, state, at("2026-10-12T12:00:00Z"))
        assertTrue(plan.occurrences.isEmpty())
        assertEquals(date("2026-10-14"), plan.plannedThroughLocalDate)
    }

    // ---- timezone changes ----

    @Test fun eastwardMoveNeverStartsBeforeThePreviousPeriodEnds() {
        val berlin = ZoneId.of("Europe/Berlin")
        val s = schedule(Frequency.DAILY, "2026-10-11")
        // Oct 11 was planned in Los Angeles: 07:00Z to 07:00Z next day.
        val state = PlanningState(date("2026-10-11"), ms("2026-10-12T07:00:00Z"))

        val before = planner.plan(s, state, at("2026-10-11T20:00:00Z", berlin))
        assertTrue(before.occurrences.isEmpty())
        val next = planner.nextOccurrence(s, state, berlin)!!
        assertEquals(date("2026-10-12"), next.startLocalDate)
        assertEquals(Instant.parse("2026-10-12T07:00:00Z"), next.startInstant)

        val after = planner.plan(s, state, at("2026-10-12T08:00:00Z", berlin))
        val row = after.occurrences.single()
        assertEquals(ms("2026-10-12T07:00:00Z"), row.startInclusiveEpochMillis) // not 22:00Z the day before
        assertEquals(ms("2026-10-12T22:00:00Z"), row.endExclusiveEpochMillis)
        assertEquals("Europe/Berlin", row.boundaryZoneId)
    }

    @Test fun westwardMoveLeavesAGapAndNamesWhenTheNextPeriodStarts() {
        val la = ZoneId.of("America/Los_Angeles")
        val s = schedule(Frequency.DAILY, "2026-10-11")
        // Oct 11 was planned in Berlin and ended 22:00Z.
        val state = PlanningState(date("2026-10-11"), ms("2026-10-11T22:00:00Z"))

        val during = planner.plan(s, state, at("2026-10-11T23:00:00Z", la))
        assertTrue(during.occurrences.isEmpty())
        assertEquals(Instant.parse("2026-10-12T07:00:00Z"), planner.nextOccurrence(s, state, la)!!.startInstant)

        val row = planner.plan(s, state, at("2026-10-12T07:00:00Z", la)).occurrences.single()
        assertEquals(ms("2026-10-12T07:00:00Z"), row.startInclusiveEpochMillis)
    }

    @Test fun dateLineMoveSkipsADateThatHasNoTimeLeft() {
        val kiritimati = ZoneId.of("Pacific/Kiritimati") // UTC+14
        val s = schedule(Frequency.DAILY, "2026-10-11")
        // Oct 11 was planned in Pago Pago (UTC-11): 11:00Z to 11:00Z next day.
        val state = PlanningState(date("2026-10-11"), ms("2026-10-12T11:00:00Z"))

        val plan = planner.plan(s, state, at("2026-10-11T12:00:00Z", kiritimati))
        assertTrue(plan.occurrences.isEmpty())
        // Oct 12 in Kiritimati ends 10:00Z, before the previous period ended: skipped, watermark moves on.
        assertEquals(date("2026-10-12"), plan.plannedThroughLocalDate)

        val skipped = state.copy(plannedThroughLocalDate = plan.plannedThroughLocalDate)
        val next = planner.nextOccurrence(s, skipped, kiritimati)!!
        assertEquals(date("2026-10-13"), next.startLocalDate)
        assertEquals(Instant.parse("2026-10-12T11:00:00Z"), next.startInstant)
    }

    // ---- daylight saving ----

    @Test fun fallBackDayIsTwentyFiveHoursLong() {
        val ny = ZoneId.of("America/New_York")
        val s = schedule(Frequency.DAILY, "2026-11-01")
        val row = planner.plan(s, none, at("2026-11-01T12:00:00Z", ny)).occurrences.single()
        assertEquals(ms("2026-11-01T04:00:00Z"), row.startInclusiveEpochMillis)
        assertEquals(ms("2026-11-02T05:00:00Z"), row.endExclusiveEpochMillis)
    }

    @Test fun springForwardDayIsTwentyThreeHoursLong() {
        val ny = ZoneId.of("America/New_York")
        val s = schedule(Frequency.DAILY, "2026-03-08")
        val row = planner.plan(s, none, at("2026-03-08T12:00:00Z", ny)).occurrences.single()
        assertEquals(ms("2026-03-08T05:00:00Z"), row.startInclusiveEpochMillis)
        assertEquals(ms("2026-03-09T04:00:00Z"), row.endExclusiveEpochMillis)
    }

    // ---- schedule validity ----

    @Test fun customScheduleNeedsWeekdaysAndOthersMustHaveNone() {
        assertThrows(IllegalArgumentException::class.java) { schedule(Frequency.CUSTOM, "2026-10-11", 0) }
        assertThrows(IllegalArgumentException::class.java) { schedule(Frequency.CUSTOM, "2026-10-11", 128) }
        assertThrows(IllegalArgumentException::class.java) { schedule(Frequency.DAILY, "2026-10-11", 21) }
    }
}
