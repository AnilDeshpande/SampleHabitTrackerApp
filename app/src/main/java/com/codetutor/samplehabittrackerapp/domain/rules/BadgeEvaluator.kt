package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Occurrence
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceStatus
import java.time.LocalDate

/**
 * The 21-Day Consistency badge: awarded once, to a Daily habit, the first time at least 18 of 21
 * consecutive calendar dates met target. A date with no row, before creation, or with a miss does
 * not qualify. Evaluating the window that ends at each newly finalized date finds the first
 * qualifying window.
 */
class BadgeEvaluator {

    /** Dates whose Daily occurrence finalized as a success. */
    fun successDates(occurrences: List<Occurrence>): Set<LocalDate> =
        occurrences
            .filter { it.status == OccurrenceStatus.FINALIZED && it.result == OccurrenceResult.SUCCESS }
            .mapTo(HashSet()) { it.startLocalDate }

    /**
     * @param occurrences the Daily habit's rows including the one just finalized
     * @param windowEnd the local date of the occurrence just finalized
     * @return true when the badge should be awarded now (for [windowEnd]); false if the habit is
     * not Daily, already has the badge, or the window does not qualify
     */
    fun shouldAward(
        frequency: Frequency,
        occurrences: List<Occurrence>,
        windowEnd: LocalDate,
        alreadyAwarded: Boolean,
    ): Boolean {
        if (frequency != Frequency.DAILY || alreadyAwarded) return false
        return qualifies(successDates(occurrences), windowEnd)
    }

    fun qualifies(successDates: Set<LocalDate>, windowEnd: LocalDate): Boolean {
        val windowStart = windowEnd.minusDays((WINDOW_DAYS - 1).toLong())
        val successes = successDates.count { !it.isBefore(windowStart) && !it.isAfter(windowEnd) }
        return successes >= REQUIRED_SUCCESSES
    }

    companion object {
        const val WINDOW_DAYS = 21
        const val REQUIRED_SUCCESSES = 18
    }
}
