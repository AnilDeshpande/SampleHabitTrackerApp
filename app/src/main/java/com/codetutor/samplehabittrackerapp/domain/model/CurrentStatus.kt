package com.codetutor.samplehabittrackerapp.domain.model

import java.math.BigDecimal
import java.time.Instant

/** What an active habit currently asks of the user, as shown on the overview. */
sealed interface CurrentStatus {
    /** One open period. */
    data class InProgress(
        val target: BigDecimal,
        val progress: BigDecimal,
        val endsAt: Instant,
    ) : CurrentStatus

    /** No period has started yet (before the first Weekly or Monthly period, or a Custom first day). */
    data class WaitingForFirstPeriod(val startsAt: Instant) : CurrentStatus

    /** Periods have run, but none is open today (for example an unselected Custom weekday). */
    data class NoOccurrenceToday(val nextStartsAt: Instant) : CurrentStatus

    /** A timezone change left a gap between two periods. */
    data class BetweenPeriods(val nextStartsAt: Instant) : CurrentStatus
}
