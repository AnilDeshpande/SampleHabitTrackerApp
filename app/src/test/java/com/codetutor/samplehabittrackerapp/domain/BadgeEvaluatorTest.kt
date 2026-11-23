package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.rules.BadgeEvaluator
import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BadgeEvaluatorTest {
    private val evaluator = BadgeEvaluator()
    private val d0 = LocalDate.parse("2026-09-01")

    private fun successes(vararg offsets: Int) = offsets.map { d0.plusDays(it.toLong()) }.toSet()

    @Test fun eighteenOfTwentyOneQualifies() {
        val dates = successes(*(0..17).toList().toIntArray())
        assertTrue(evaluator.qualifies(dates, d0.plusDays(20)))
    }

    @Test fun seventeenOfTwentyOneDoesNot() {
        val dates = successes(*(0..16).toList().toIntArray())
        assertFalse(evaluator.qualifies(dates, d0.plusDays(20)))
    }

    @Test fun windowBoundaryIsTwentyOneDatesInclusive() {
        val dates = successes(*(0..17).toList().toIntArray()) // 18 successes on days 0..17
        assertTrue(evaluator.qualifies(dates, d0.plusDays(20))) // window days 0..20 contains all 18
        assertFalse(evaluator.qualifies(dates, d0.plusDays(21))) // window days 1..21 contains only 17
    }

    @Test fun missesGapsAndDatesBeforeCreationDoNotQualify() {
        // 18 successes spread over 22 dates, so no 21-date window holds 18 of them.
        val spread = successes(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 21)
        assertFalse(evaluator.qualifies(spread, d0.plusDays(21)))
        assertFalse(evaluator.qualifies(spread, d0.plusDays(20)))
    }

    @Test fun onlyFinalizedSuccessRowsCountAsSuccessDates() {
        val rows = listOf(
            dailyOccurrence(1, d0, OccurrenceResult.SUCCESS),
            dailyOccurrence(2, d0.plusDays(1), OccurrenceResult.MISS),
            dailyOccurrence(3, d0.plusDays(2), null),
            dailyOccurrence(4, d0.plusDays(3), OccurrenceResult.CANCELLED),
        )
        assertTrue(evaluator.successDates(rows) == setOf(d0))
    }

    @Test fun shouldAwardWhenTheNewlyFinalizedDateCompletesAQualifyingWindow() {
        val rows = (0..17).map { dailyOccurrence(it + 1L, d0.plusDays(it.toLong()), OccurrenceResult.SUCCESS) }
        assertTrue(evaluator.shouldAward(Frequency.DAILY, rows, d0.plusDays(17), alreadyAwarded = false))
    }

    @Test fun awardsOnlyOnceAndOnlyForDailyHabits() {
        val rows = (0..17).map { dailyOccurrence(it + 1L, d0.plusDays(it.toLong()), OccurrenceResult.SUCCESS) }
        assertFalse(evaluator.shouldAward(Frequency.DAILY, rows, d0.plusDays(17), alreadyAwarded = true))
        for (frequency in listOf(Frequency.WEEKLY, Frequency.FORTNIGHTLY, Frequency.MONTHLY, Frequency.CUSTOM)) {
            assertFalse(evaluator.shouldAward(frequency, rows, d0.plusDays(17), alreadyAwarded = false))
        }
    }

    @Test fun aMissFinalizationCanStillCompleteAWindowThatTheLastFinalizedDayMissed() {
        // 18 successes on days 0..17, then day 19 finalizes as a miss while day 18 has no row.
        val rows = (0..17).map { dailyOccurrence(it + 1L, d0.plusDays(it.toLong()), OccurrenceResult.SUCCESS) } +
            dailyOccurrence(99, d0.plusDays(19), OccurrenceResult.MISS)
        assertTrue(evaluator.shouldAward(Frequency.DAILY, rows, d0.plusDays(19), alreadyAwarded = false))
    }
}
