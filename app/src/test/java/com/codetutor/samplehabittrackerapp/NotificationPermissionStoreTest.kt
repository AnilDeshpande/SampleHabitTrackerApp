package com.codetutor.samplehabittrackerapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.codetutor.samplehabittrackerapp.data.preferences.NotificationPermissionStore
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NotificationPermissionStoreTest {
    @get:Rule
    val folder = TemporaryFolder()

    private fun open(file: File, scope: CoroutineScope) =
        NotificationPermissionStore(PreferenceDataStoreFactory.create(scope = scope) { file })

    @Test
    fun requestedFlagPersistsAfterReopeningTheFile() = runBlocking {
        val file = File(folder.root, "settings.preferences_pb")

        val firstScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val first = open(file, firstScope)
        assertFalse(first.requested.first())
        first.markRequested()
        assertTrue(first.requested.first())
        firstScope.cancel()

        val secondScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        assertTrue(open(file, secondScope).requested.first())
        secondScope.cancel()
    }
}
