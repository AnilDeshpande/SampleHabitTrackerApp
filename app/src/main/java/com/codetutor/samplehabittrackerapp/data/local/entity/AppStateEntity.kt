package com.codetutor.samplehabittrackerapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Singleton row (always id 1) holding the app-wide points balance. */
@Entity(tableName = "app_state")
data class AppStateEntity(
    @PrimaryKey val appStateId: Long = SINGLETON_ID,
    val pointsBalance: Long,
) {
    companion object {
        const val SINGLETON_ID = 1L
    }
}
