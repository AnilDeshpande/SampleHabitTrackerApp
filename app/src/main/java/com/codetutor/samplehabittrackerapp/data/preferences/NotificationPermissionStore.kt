package com.codetutor.samplehabittrackerapp.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/** The only DataStore value: whether the notification permission was ever requested. */
@Singleton
class NotificationPermissionStore @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    val requested: Flow<Boolean> = dataStore.data.map { it[KEY] ?: false }

    suspend fun markRequested() {
        dataStore.edit { it[KEY] = true }
    }

    private companion object {
        val KEY = booleanPreferencesKey("notificationPermissionRequested")
    }
}
