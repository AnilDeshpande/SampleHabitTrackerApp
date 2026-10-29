package com.codetutor.samplehabittrackerapp

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Host-tier SQL smoke. Host SQLite is not minimum-API evidence; the instrumented tiers are. */
@RunWith(RobolectricTestRunner::class)
class DatabaseTest {
    @Test
    fun fileBackedDatabaseIsWalAndSurvivesReopen() = runBlocking {
        val evidence = DatabaseSmoke.verifyFileBackedReopen(ApplicationProvider.getApplicationContext(), "smoke-host.db")
        println("SqlEvidence(host): $evidence")
    }
}
