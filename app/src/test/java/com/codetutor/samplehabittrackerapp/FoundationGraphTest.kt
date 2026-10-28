package com.codetutor.samplehabittrackerapp

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import com.codetutor.samplehabittrackerapp.data.local.HabitTrackerDatabase
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.TimeZone
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class FoundationGraphTest {
    @get:Rule val hilt = HiltAndroidRule(this)
    @Inject lateinit var clock: Clock
    @Inject lateinit var dispatchers: AppDispatchers
    @Inject lateinit var databaseProvider: DatabaseProvider
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Test fun headlessGraphIsLazyAndConcurrentDatabaseCallersShareOneInstance() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(HabitTrackerDatabase.FILE_NAME)
        hilt.inject()
        assertFalse(context.getDatabasePath(HabitTrackerDatabase.FILE_NAME).exists())
        assertNotNull(workerFactory)
        assertNotNull(dispatchers.io)
        try {
            val databases = List(8) { async { databaseProvider.get() } }.awaitAll()
            databases.forEach { assertSame(databases.first(), it) }
            assertEquals(0L, databases.first().trackingDao().gateVersion())
        } finally {
            databaseProvider.close()
            context.deleteDatabase(HabitTrackerDatabase.FILE_NAME)
        }
    }

    @Test fun singletonClockReadsCurrentZoneInsteadOfCachingStartupZone() {
        hilt.inject()
        val original = TimeZone.getDefault()
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("Asia/Kolkata"))
            assertEquals("Asia/Kolkata", clock.zone().id)
            TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"))
            assertEquals("America/New_York", clock.zone().id)
        } finally {
            TimeZone.setDefault(original)
        }
    }
}
