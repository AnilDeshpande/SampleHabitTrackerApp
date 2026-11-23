package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import java.math.BigDecimal

/** [pointsEffect] is signed; [balanceAfter] is never negative. */
data class ScoreOutcome(val pointsEffect: Long, val balanceAfter: Long)

/**
 * Integer-only scoring. A success adds [SUCCESS_POINTS]. A miss deducts 1% of the current balance
 * rounded up, which is a one-point minimum while the balance is positive, and never takes the
 * balance below zero.
 */
class ScoringEngine {

    fun resultFor(progress: BigDecimal, effectiveTarget: BigDecimal): OccurrenceResult =
        if (progress.compareTo(effectiveTarget) >= 0) OccurrenceResult.SUCCESS else OccurrenceResult.MISS

    fun score(result: OccurrenceResult, balanceBefore: Long): ScoreOutcome {
        require(balanceBefore >= 0) { "Balance cannot be negative" }
        return when (result) {
            OccurrenceResult.SUCCESS -> {
                val after = if (balanceBefore > Long.MAX_VALUE - SUCCESS_POINTS) Long.MAX_VALUE else balanceBefore + SUCCESS_POINTS
                ScoreOutcome(after - balanceBefore, after)
            }

            OccurrenceResult.MISS -> {
                val deduction = if (balanceBefore == 0L) 0L else ceilPercent(balanceBefore)
                ScoreOutcome(-deduction, balanceBefore - deduction)
            }

            OccurrenceResult.CANCELLED -> throw IllegalArgumentException("A cancelled occurrence has no score")
        }
    }

    // ceil(balance / 100) without the overflow that (balance + 99) / 100 has near Long.MAX_VALUE.
    private fun ceilPercent(balance: Long): Long = balance / 100 + if (balance % 100 != 0L) 1 else 0

    companion object {
        const val SUCCESS_POINTS = 10L
    }
}
