package com.codetutor.samplehabittrackerapp

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Proves Mockk's agent works on the host JVM. Per the LLD, Mockk is reserved for external-effect
 * ports (notifier, work scheduler, permission/channel state) and ViewModel UI-mapping tests, so
 * this uses a test-local stand-in port rather than any production type; those ports do not exist yet.
 */
class MockkSmokeTest {
    private interface EffectPort {
        fun post(identity: Int): Boolean
    }

    @Test
    fun mocksAnExternalEffectPortAndVerifiesCalls() {
        val port = mockk<EffectPort>()
        every { port.post(7) } returns true

        assertTrue(port.post(7))
        verify(exactly = 1) { port.post(7) }
    }
}
