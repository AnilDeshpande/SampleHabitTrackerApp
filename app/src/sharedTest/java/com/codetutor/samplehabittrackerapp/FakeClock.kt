package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.core.time.Clock
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler

@OptIn(ExperimentalCoroutinesApi::class)
class FakeClock(
    private val scheduler: TestCoroutineScheduler,
    private val base: Instant = Instant.parse("2026-10-28T00:00:00Z"),
    var currentZone: ZoneId = ZoneId.of("UTC"),
) : Clock {
    override fun now(): Instant = base.plusMillis(scheduler.currentTime)
    override fun zone(): ZoneId = currentZone
}
