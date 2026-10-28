package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.core.time.TimeSnapshot
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TimeSnapshotTest {
    @Test fun snapshotIsImmutableWhileVirtualTimeAndZoneAdvance() = runTest {
        val clock = FakeClock(testScheduler)
        val before = TimeSnapshot.capture(clock)
        advanceTimeBy(1_000)
        clock.currentZone = ZoneId.of("America/Los_Angeles")
        val after = TimeSnapshot.capture(clock)
        assertEquals(1_000, after.instant.toEpochMilli() - before.instant.toEpochMilli())
        assertEquals(LocalDate.of(2026, 10, 28), before.localDate)
        assertEquals(LocalDate.of(2026, 10, 27), after.localDate)
    }
}
