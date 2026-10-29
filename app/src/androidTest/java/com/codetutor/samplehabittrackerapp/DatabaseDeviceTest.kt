package com.codetutor.samplehabittrackerapp

import android.os.Bundle
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Platform-SQLite smoke. Passing on API 24 is the minimum-API evidence tier; the API level
 * and sqlite_version() are logged and sent as instrumentation status for the test report.
 */
@RunWith(AndroidJUnit4::class)
class DatabaseDeviceTest {
    @Test
    fun fileBackedDatabaseIsWalAndSurvivesReopenOnPlatformSqlite() = runBlocking {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val evidence = DatabaseSmoke.verifyFileBackedReopen(instrumentation.targetContext, "smoke-device.db")
        Log.i("SqlEvidence", evidence.toString())
        instrumentation.sendStatus(0, Bundle().apply { putString("SqlEvidence", evidence.toString()) })
    }
}
