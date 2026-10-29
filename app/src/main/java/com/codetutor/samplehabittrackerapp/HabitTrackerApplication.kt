package com.codetutor.samplehabittrackerapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.codetutor.samplehabittrackerapp.data.notification.ensureMissedTargetsChannel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    // On-demand WorkManager initialization; the default initializer is removed in the manifest.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        ensureMissedTargetsChannel(this)
    }
}
