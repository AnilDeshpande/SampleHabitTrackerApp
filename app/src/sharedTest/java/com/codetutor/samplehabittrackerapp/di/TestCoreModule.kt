@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp.di

import com.codetutor.samplehabittrackerapp.FakeClock
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.core.time.Clock
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler

/** Test graph: one shared virtual-time scheduler drives the clock, dispatchers and app scope. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [CoreModule::class])
object TestCoreModule {
    @Provides
    @Singleton
    fun scheduler(): TestCoroutineScheduler = TestCoroutineScheduler()

    @Provides
    @Singleton
    fun fakeClock(scheduler: TestCoroutineScheduler): FakeClock = FakeClock(scheduler)

    @Provides
    @Singleton
    fun clock(fakeClock: FakeClock): Clock = fakeClock

    @Provides
    @Singleton
    fun dispatchers(scheduler: TestCoroutineScheduler): AppDispatchers {
        val dispatcher = StandardTestDispatcher(scheduler)
        return AppDispatchers(io = dispatcher, default = dispatcher, main = dispatcher)
    }

    @Provides
    @Singleton
    @ApplicationScope
    fun applicationScope(dispatchers: AppDispatchers): CoroutineScope =
        CoroutineScope(SupervisorJob() + dispatchers.default)
}
