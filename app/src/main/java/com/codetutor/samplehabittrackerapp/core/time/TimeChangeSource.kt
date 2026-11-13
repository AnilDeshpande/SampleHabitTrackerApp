package com.codetutor.samplehabittrackerapp.core.time

import kotlinx.coroutines.flow.Flow

/**
 * Emits when the system reports a clock, date or timezone change. A cold flow: the Android adapter
 * registers its receiver while collected, so nothing is registered while the app is stopped.
 */
interface TimeChangeSource {
    val events: Flow<Unit>
}
