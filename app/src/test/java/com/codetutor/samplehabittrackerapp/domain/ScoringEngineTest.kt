package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.rules.ScoreOutcome
import com.codetutor.samplehabittrackerapp.domain.rules.ScoringEngine
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ScoringEngineTest {
    private val engine = ScoringEngine()

    private fun miss(balance: Long) = engine.score(OccurrenceResult.MISS, balance)

    @Test fun successAddsTenPoints() {
        assertEquals(ScoreOutcome(10, 10), engine.score(OccurrenceResult.SUCCESS, 0))
        assertEquals(ScoreOutcome(10, 180), engine.score(OccurrenceResult.SUCCESS, 170))
    }

    @Test fun missDeductsOnePercentRoundedUp() {
        assertEquals(ScoreOutcome(-1, 99), miss(100))
        assertEquals(ScoreOutcome(-2, 99), miss(101))
        assertEquals(ScoreOutcome(-2, 197), miss(199))
        assertEquals(ScoreOutcome(-2, 198), miss(200))
        assertEquals(ScoreOutcome(-3, 198), miss(201))
        assertEquals(ScoreOutcome(-10, 990), miss(1000))
    }

    @Test fun missHasAOnePointMinimumWhileBalanceIsPositive() {
        assertEquals(ScoreOutcome(-1, 9), miss(10))
        assertEquals(ScoreOutcome(-1, 0), miss(1))
    }

    @Test fun missNeverTakesTheBalanceBelowZero() {
        assertEquals(ScoreOutcome(0, 0), miss(0))
    }

    @Test fun twoCatchUpMissesFromOneHundredLeaveNinetyEight() {
        val first = miss(100)
        val second = miss(first.balanceAfter)
        assertEquals(98, second.balanceAfter)
    }

    @Test fun roundingDoesNotOverflowNearTheLimit() {
        val outcome = miss(Long.MAX_VALUE)
        assertEquals(Long.MAX_VALUE - (Long.MAX_VALUE / 100 + 1), outcome.balanceAfter)
        assertEquals(ScoreOutcome(0, Long.MAX_VALUE), engine.score(OccurrenceResult.SUCCESS, Long.MAX_VALUE))
    }

    @Test fun cancelledOccurrencesAndNegativeBalancesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { engine.score(OccurrenceResult.CANCELLED, 10) }
        assertThrows(IllegalArgumentException::class.java) { engine.score(OccurrenceResult.MISS, -1) }
    }

    @Test fun targetIsMetWhenProgressEqualsOrExceedsIt() {
        assertEquals(OccurrenceResult.SUCCESS, engine.resultFor(BigDecimal("10"), BigDecimal("10.0")))
        assertEquals(OccurrenceResult.SUCCESS, engine.resultFor(BigDecimal("12"), BigDecimal("10")))
        assertEquals(OccurrenceResult.MISS, engine.resultFor(BigDecimal("9.999"), BigDecimal("10")))
        assertEquals(OccurrenceResult.MISS, engine.resultFor(BigDecimal.ZERO, BigDecimal("0.001")))
    }
}
