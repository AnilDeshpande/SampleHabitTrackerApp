package com.codetutor.samplehabittrackerapp.domain.model

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

/** What an active habit currently asks of the user, as shown on the overview. */
sealed interface CurrentStatus {
    /**
     * One open period. [startLocalDate] and [endLocalDate] are the stored period's local dates, inclusive; they
     * identify the period the progress belongs to even after a timezone change. [endsAt] is its stored end instant.
     */
    data class InProgress(
        val target: BigDecimal,
        val progress: BigDecimal,
        val startLocalDate: LocalDate,
        val endLocalDate: LocalDate,
        val endsAt: Instant,
    ) : CurrentStatus

    /** No period has started yet (before the first Weekly or Monthly period, or a Custom first day). */
    data class WaitingForFirstPeriod(val startsAt: Instant) : CurrentStatus

    /** Periods have run, but none is open today (for example an unselected Custom weekday). */
    data class NoOccurrenceToday(val nextStartsAt: Instant) : CurrentStatus

    /** A timezone change left a gap between two periods. */
    data class BetweenPeriods(val nextStartsAt: Instant) : CurrentStatus
}
