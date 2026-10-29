package com.codetutor.samplehabittrackerapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.codetutor.samplehabittrackerapp.data.local.DatabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

/** Storage locations. Replaced wholesale in the test graph (in-memory / temporary files). */
@Module
@InstallIn(SingletonComponent::class)
object PersistenceModule {
    @Provides
    fun databaseConfig(): DatabaseConfig = DatabaseConfig(fileName = "habit_tracker.db")

    @Provides
    @Singleton
    fun preferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope) {
        context.preferencesDataStoreFile("settings")
    }
}
