package com.codetutor.samplehabittrackerapp.data.notification

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import com.codetutor.samplehabittrackerapp.domain.port.NotificationAvailability
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * `areNotificationsEnabled()` already folds in the API 33+ runtime permission and an app-level block;
 * a channel switched off to IMPORTANCE_NONE is treated the same way.
 */
@Singleton
class AndroidNotificationAvailability @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationAvailability {
    override fun canPostMissedTargetNotifications(): Boolean {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        val channel = manager.getNotificationChannelCompat(MISSED_TARGETS_CHANNEL_ID)
        return channel == null || channel.importance != NotificationManagerCompat.IMPORTANCE_NONE
    }
}
