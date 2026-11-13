package com.codetutor.samplehabittrackerapp.domain.model

import java.math.BigDecimal
import java.time.LocalDate

enum class OccurrenceStatus { OPEN, FINALIZED, CANCELLED }

enum class OccurrenceResult { SUCCESS, MISS, CANCELLED }

/**
 * One tracked period of one habit. A row is created only once its period starts, stores its own
 * effective target, and never changes its boundaries. [endLocalDate] is the period's last local
 * date, inclusive; the period ends exclusively at the start of the day after it.
 */
data class Occurrence(
    val occurrenceId: Long,
    val habitId: Long,
    val startLocalDate: LocalDate,
    val endLocalDate: LocalDate,
    val boundaryZoneId: String,
    val startInclusiveEpochMillis: Long,
    val endExclusiveEpochMillis: Long,
    val effectiveTarget: BigDecimal,
    val progress: BigDecimal,
    val status: OccurrenceStatus,
    val pointsEffect: Long,
    val result: OccurrenceResult?,
    val resolvedAtEpochMillis: Long?,
    val missAcknowledgedAtEpochMillis: Long?,
)
