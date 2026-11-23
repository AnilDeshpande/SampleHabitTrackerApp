package com.codetutor.samplehabittrackerapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import com.codetutor.samplehabittrackerapp.core.lifecycle.ForegroundCoordinator
import com.codetutor.samplehabittrackerapp.data.notification.ensureMissedTargetsChannel
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class HabitTrackerApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var foregroundCoordinator: ForegroundCoordinator

    // On-demand WorkManager initialization; the default initializer is removed in the manifest.
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        ensureMissedTargetsChannel(this)
        // The one process-lifecycle observer: catch-up and the boundary ticker follow the app's
        // foreground state. Instrumented tests start HiltTestApplication, so this is production only.
        foregroundCoordinator.install(ProcessLifecycleOwner.get().lifecycle)
    }
}
