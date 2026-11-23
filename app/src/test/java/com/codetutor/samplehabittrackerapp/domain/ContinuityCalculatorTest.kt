package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult.CANCELLED
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult.MISS
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult.SUCCESS
import com.codetutor.samplehabittrackerapp.domain.rules.Continuity
import com.codetutor.samplehabittrackerapp.domain.rules.ContinuityCalculator
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

class ContinuityCalculatorTest {
    private val calculator = ContinuityCalculator()
    private val day1 = LocalDate.parse("2026-10-01")

    private fun rows(vararg results: com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult?) =
        results.mapIndexed { i, r -> dailyOccurrence(i + 1L, day1.plusDays(i.toLong()), r) }

    @Test fun noRowsMeansZeroRunAndStreak() {
        assertEquals(Continuity(0, 0), calculator.calculate(emptyList()))
    }

    @Test fun openRowCountsTowardRunButNotStreak() {
        assertEquals(Continuity(1, 0), calculator.calculate(rows(null)))
        assertEquals(Continuity(2, 1), calculator.calculate(rows(SUCCESS, null)))
    }

    @Test fun missResetsStreakButNotRun() {
        // Mirrors the prototype's S67: two misses, run continues at 11 with streak 0.
        val results = Array(9) { SUCCESS } + arrayOf(MISS, MISS)
        assertEquals(Continuity(11, 0), calculator.calculate(rows(*results)))
    }

    @Test fun streakCountsSuccessesBackFromTheNewestUntilAMiss() {
        assertEquals(Continuity(5, 2), calculator.calculate(rows(SUCCESS, MISS, SUCCESS, SUCCESS, null)))
    }

    @Test fun cancelledRowsHaveNoContinuityEffect() {
        assertEquals(Continuity(2, 2), calculator.calculate(rows(SUCCESS, SUCCESS, CANCELLED)))
    }

    @Test fun orderingComesFromTheRowsNotTheListOrder() {
        val shuffled = rows(SUCCESS, MISS, SUCCESS).reversed()
        assertEquals(Continuity(3, 1), calculator.calculate(shuffled))
    }
}
