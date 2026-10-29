@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.core.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler

/** A base instant plus the shared test scheduler's virtual time; the zone is controllable. */
class FakeClock(
    private val scheduler: TestCoroutineScheduler,
    private val base: Instant = Instant.parse("2026-01-05T09:00:00Z"),
    var currentZone: ZoneId = ZoneOffset.UTC,
) : Clock {
    override fun now(): Instant = base.plusMillis(scheduler.currentTime)
    override fun zone(): ZoneId = currentZone
}
