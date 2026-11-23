package com.codetutor.samplehabittrackerapp.domain.port

/**
 * Whether the device will display this app's notifications. Plain values only: Android types stay
 * in the adapter. A denied permission and a blocked "Missed targets" channel are treated alike,
 * and never stop tracking.
 */
interface NotificationAvailability {
    fun canPostMissedTargetNotifications(): Boolean
}
