package com.codetutor.samplehabittrackerapp.domain

import com.codetutor.samplehabittrackerapp.domain.model.Occurrence
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

fun ms(isoInstant: String): Long = Instant.parse(isoInstant).toEpochMilli()

/** A one-day UTC occurrence on [date]; finalized with [result] unless [status] says otherwise. */
fun dailyOccurrence(
    id: Long,
    date: LocalDate,
    result: OccurrenceResult?,
    status: OccurrenceStatus = when (result) {
        null -> OccurrenceStatus.OPEN
        OccurrenceResult.CANCELLED -> OccurrenceStatus.CANCELLED
        else -> OccurrenceStatus.FINALIZED
    },
): Occurrence {
    val start = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    return Occurrence(
        occurrenceId = id,
        habitId = 1,
        startLocalDate = date,
        endLocalDate = date,
        boundaryZoneId = "UTC",
        startInclusiveEpochMillis = start,
        endExclusiveEpochMillis = start + 86_400_000L,
        effectiveTarget = BigDecimal.TEN,
        progress = BigDecimal.ZERO,
        status = status,
        pointsEffect = 0,
        result = result,
        resolvedAtEpochMillis = if (status == OccurrenceStatus.OPEN) null else start,
        missAcknowledgedAtEpochMillis = null,
    )
}
