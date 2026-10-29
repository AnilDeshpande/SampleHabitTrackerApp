package com.codetutor.samplehabittrackerapp

import com.codetutor.samplehabittrackerapp.core.time.Clock
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

/** Proves Mockk's agent works on the host JVM; real uses are limited to external-effect ports. */
class MockkSmokeTest {
    @Test
    fun mocksAnInterfaceAndVerifiesCalls() {
        val clock = mockk<Clock>()
        every { clock.now() } returns Instant.EPOCH

        assertEquals(Instant.EPOCH, clock.now())
        verify(exactly = 1) { clock.now() }
    }
}
