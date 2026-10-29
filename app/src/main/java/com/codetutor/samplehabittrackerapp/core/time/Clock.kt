package com.codetutor.samplehabittrackerapp.core.time

import java.time.Instant
import java.time.ZoneId

/** Android-free time source; the current zone is read on every call. */
interface Clock {
    fun now(): Instant
    fun zone(): ZoneId
}

class DeviceClock : Clock {
    override fun now(): Instant = Instant.now()
    override fun zone(): ZoneId = ZoneId.systemDefault()
}
