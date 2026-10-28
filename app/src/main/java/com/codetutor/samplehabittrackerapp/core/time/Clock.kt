package com.codetutor.samplehabittrackerapp.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

interface Clock {
    fun now(): Instant
    fun zone(): ZoneId
}

class SystemClock : Clock {
    private val utc = java.time.Clock.systemUTC()
    override fun now(): Instant = utc.instant()
    override fun zone(): ZoneId = TimeZone.getDefault().toZoneId()
}

data class TimeSnapshot(val instant: Instant, val zone: ZoneId) {
    val localDate: LocalDate get() = instant.atZone(zone).toLocalDate()

    companion object {
        /** Call only after acquiring the database gate for tracking commands. */
        fun capture(clock: Clock): TimeSnapshot = TimeSnapshot(clock.now(), clock.zone())
    }
}
