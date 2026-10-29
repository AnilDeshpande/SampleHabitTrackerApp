package com.codetutor.samplehabittrackerapp

import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorkerFactory
import androidx.test.core.app.ApplicationProvider
import androidx.work.WorkManager
import com.codetutor.samplehabittrackerapp.data.notification.MISSED_TARGETS_CHANNEL_ID
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Runs the real production Application (no test graph) against the merged manifest. */
@RunWith(RobolectricTestRunner::class)
class ApplicationWiringTest {
    private val app get() = ApplicationProvider.getApplicationContext<HabitTrackerApplication>()

    @Test
    fun workManagerConfigurationUsesTheHiltWorkerFactory() {
        assertTrue(app.workManagerConfiguration.workerFactory is HiltWorkerFactory)
    }

    @Test
    fun workManagerInitializesOnDemandFromThatConfiguration() {
        assertNotNull(WorkManager.getInstance(app))
    }

    @Test
    fun onlyTheWorkManagerStartupInitializerIsRemoved() {
        val provider = app.packageManager.getProviderInfo(
            ComponentName(app.packageName, "androidx.startup.InitializationProvider"),
            PackageManager.GET_META_DATA,
        )
        val initializers = provider.metaData.keySet()
        assertFalse(initializers.contains("androidx.work.WorkManagerInitializer"))
        assertTrue(initializers.contains("androidx.lifecycle.ProcessLifecycleInitializer"))
    }

    @Test
    fun missedTargetsChannelIsCreatedAtStartup() {
        val channel = NotificationManagerCompat.from(app).getNotificationChannelCompat(MISSED_TARGETS_CHANNEL_ID)
        assertNotNull(channel)
        assertEquals("Missed targets", channel!!.name.toString())
    }
}
