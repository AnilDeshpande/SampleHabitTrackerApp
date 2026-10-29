@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Test

class FakeClockTest {
    @Test
    fun virtualTimeAdvancesTheClockAndTheDelaysTogether() = runTest(TestCoroutineScheduler()) {
        val clock = FakeClock(testScheduler)
        val start = clock.now()
        var fired: Instant? = null
        val dispatcher = StandardTestDispatcher(testScheduler)

        kotlinx.coroutines.CoroutineScope(dispatcher).launch {
            delay(60_000)
            fired = clock.now()
        }
        testScheduler.advanceTimeBy(60_001)
        testScheduler.runCurrent()

        assertEquals(start.plus(Duration.ofMinutes(1)), fired)
    }

    @Test
    fun zoneIsControllable() {
        val clock = FakeClock(TestCoroutineScheduler())
        clock.currentZone = ZoneId.of("Asia/Kolkata")
        assertEquals(ZoneId.of("Asia/Kolkata"), clock.zone())
    }
}
