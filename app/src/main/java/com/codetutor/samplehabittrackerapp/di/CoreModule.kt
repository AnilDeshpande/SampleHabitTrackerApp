package com.codetutor.samplehabittrackerapp.di

import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.core.time.DeviceClock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

/** Process-wide time and concurrency seams. Replaced wholesale in the test graph. */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun clock(): Clock = DeviceClock()

    @Provides
    @Singleton
    fun dispatchers(): AppDispatchers = AppDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main)

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(dispatchers: AppDispatchers): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.default)
}
