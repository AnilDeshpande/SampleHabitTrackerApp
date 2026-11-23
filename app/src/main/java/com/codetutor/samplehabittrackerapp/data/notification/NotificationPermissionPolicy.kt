package com.codetutor.samplehabittrackerapp.data.notification

import android.os.Build
import com.codetutor.samplehabittrackerapp.data.preferences.NotificationPermissionStore
import com.codetutor.samplehabittrackerapp.domain.port.NotificationAvailability
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first

/**
 * When to ask for the notification permission (PRD: request once, after the first successful habit
 * creation, on API 33+; never again automatically). Denial never blocks tracking: misses stay visible
 * in the app.
 */
@Singleton
class NotificationPermissionPolicy @Inject constructor(
    private val store: NotificationPermissionStore,
    private val availability: NotificationAvailability,
) {
    /**
     * True exactly once per install: on API 33+, the first time this is asked after a creation, if
     * the request was never made and notifications are not already allowed. Records the request, so a
     * later call (or a restart) never asks again.
     */
    suspend fun consumeRequestAfterCreation(sdkInt: Int = Build.VERSION.SDK_INT): Boolean {
        if (sdkInt < Build.VERSION_CODES.TIRAMISU) return false
        if (store.requested.first()) return false
        store.markRequested()
        return !availability.canPostMissedTargetNotifications()
    }
}
