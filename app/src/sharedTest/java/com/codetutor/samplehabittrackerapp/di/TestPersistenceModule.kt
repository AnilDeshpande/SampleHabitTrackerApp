package com.codetutor.samplehabittrackerapp.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.codetutor.samplehabittrackerapp.data.local.DatabaseConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import java.io.File
import java.util.UUID
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

/** Test storage: in-memory Room and a unique temporary DataStore file per graph. */
@Module
@TestInstallIn(components = [SingletonComponent::class], replaces = [PersistenceModule::class])
object TestPersistenceModule {
    @Provides
    fun databaseConfig(): DatabaseConfig = DatabaseConfig(fileName = null)

    @Provides
    @Singleton
    fun preferencesDataStore(
        @ApplicationContext context: Context,
        @ApplicationScope scope: CoroutineScope,
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create(scope = scope) {
        File(context.cacheDir, "test-${UUID.randomUUID()}.preferences_pb")
    }
}
