package com.codetutor.samplehabittrackerapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.codetutor.samplehabittrackerapp.data.preferences.NotificationPermissionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NotificationPermissionStoreTest {
    @get:Rule val folder = TemporaryFolder()

    @Test fun permissionFlagSurvivesStoreRecreation() = runBlocking {
        val file = folder.root.resolve("permission.preferences_pb")
        suspend fun useStore(action: suspend (NotificationPermissionStore) -> Unit) {
            val job = SupervisorJob()
            val scope = CoroutineScope(job + Dispatchers.IO)
            val dataStore = PreferenceDataStoreFactory.create(scope = scope, produceFile = { file })
            try {
                action(NotificationPermissionStore(dataStore))
            } finally {
                scope.cancel()
                job.join()
            }
        }
        useStore {
            assertFalse(it.requested.first())
            it.markRequested()
            assertTrue(it.requested.first())
        }
        useStore { assertTrue(it.requested.first()) }
    }
}
