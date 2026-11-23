package com.codetutor.samplehabittrackerapp.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * One consistent reading of the clock: the instant, the zone, and the local date they imply.
 * A command captures exactly one snapshot after it holds the database gate and uses it for every
 * period decision in that transaction.
 */
data class TimeSnapshot(val instant: Instant, val zone: ZoneId) {
    val epochMillis: Long get() = instant.toEpochMilli()
    val localDate: LocalDate get() = instant.atZone(zone).toLocalDate()
}

fun Clock.snapshot(): TimeSnapshot = TimeSnapshot(now(), zone())
