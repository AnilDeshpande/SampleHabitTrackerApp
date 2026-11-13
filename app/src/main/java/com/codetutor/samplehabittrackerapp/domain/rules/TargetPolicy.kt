package com.codetutor.samplehabittrackerapp.domain.rules

import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import java.time.LocalDate

/** Where a saved target change takes effect. */
sealed interface TargetEffect {
    val effectiveStartLocalDate: LocalDate

    /** Daily or Custom with an open occurrence: that occurrence's effective target is updated now. */
    data class AppliesToCurrentOccurrence(override val effectiveStartLocalDate: LocalDate) : TargetEffect

    /** Everything else: the next occurrence that has not started carries the new target. */
    data class AppliesFromNextOccurrence(override val effectiveStartLocalDate: LocalDate) : TargetEffect
}

/** Cadence-specific target-change timing from the PRD. It never touches finalized rows. */
class TargetPolicy {

    /**
     * @param currentOpenOccurrenceStartLocalDate start date of the habit's open occurrence at the
     * command's snapshot, or null when none is open.
     * @param nextOccurrenceStartLocalDate start date of the next occurrence that has not started.
     */
    fun resolve(
        frequency: Frequency,
        currentOpenOccurrenceStartLocalDate: LocalDate?,
        nextOccurrenceStartLocalDate: LocalDate,
    ): TargetEffect {
        val immediate = frequency == Frequency.DAILY || frequency == Frequency.CUSTOM
        return if (immediate && currentOpenOccurrenceStartLocalDate != null) {
            TargetEffect.AppliesToCurrentOccurrence(currentOpenOccurrenceStartLocalDate)
        } else {
            TargetEffect.AppliesFromNextOccurrence(nextOccurrenceStartLocalDate)
        }
    }
}
