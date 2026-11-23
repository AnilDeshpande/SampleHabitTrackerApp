package com.codetutor.samplehabittrackerapp.di

import com.codetutor.samplehabittrackerapp.core.time.TimeChangeSource
import com.codetutor.samplehabittrackerapp.data.notification.AndroidNotificationAvailability
import com.codetutor.samplehabittrackerapp.data.platform.AndroidTimeChangeSource
import com.codetutor.samplehabittrackerapp.domain.port.NotificationAvailability
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Android adapters behind plain ports, so tests substitute recording or scripted fakes. */
@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {
    @Binds abstract fun timeChangeSource(impl: AndroidTimeChangeSource): TimeChangeSource
    @Binds abstract fun notificationAvailability(impl: AndroidNotificationAvailability): NotificationAvailability
}
