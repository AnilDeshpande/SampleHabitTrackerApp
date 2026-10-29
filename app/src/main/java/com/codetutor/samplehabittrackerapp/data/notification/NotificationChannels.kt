package com.codetutor.samplehabittrackerapp.data.notification

import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationManagerCompat
import com.codetutor.samplehabittrackerapp.R

const val MISSED_TARGETS_CHANNEL_ID = "missed_targets"

/** Creates the "Missed targets" channel; a no-op below API 26. Safe to call repeatedly. */
fun ensureMissedTargetsChannel(context: Context) {
    val channel = NotificationChannelCompat.Builder(
        MISSED_TARGETS_CHANNEL_ID,
        NotificationManagerCompat.IMPORTANCE_DEFAULT,
    ).setName(context.getString(R.string.channel_missed_targets_name)).build()
    NotificationManagerCompat.from(context).createNotificationChannel(channel)
}
