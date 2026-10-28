package com.codetutor.samplehabittrackerapp.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** Presentation preference only; tracking truth belongs in Room. Read failures propagate. */
@Singleton
class NotificationPermissionStore @Inject constructor(private val store: DataStore<Preferences>) {
    private val requestedKey = booleanPreferencesKey("notificationPermissionRequested")
    val requested: Flow<Boolean> = store.data.map { it[requestedKey] ?: false }

    suspend fun markRequested() {
        store.edit { it[requestedKey] = true }
    }
}
