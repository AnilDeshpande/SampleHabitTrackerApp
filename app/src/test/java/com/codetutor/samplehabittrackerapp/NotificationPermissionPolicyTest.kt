package com.codetutor.samplehabittrackerapp

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.codetutor.samplehabittrackerapp.data.notification.NotificationPermissionPolicy
import com.codetutor.samplehabittrackerapp.data.preferences.NotificationPermissionStore
import com.codetutor.samplehabittrackerapp.domain.port.NotificationAvailability
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class NotificationPermissionPolicyTest {
    @get:Rule val folder = TemporaryFolder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var store: NotificationPermissionStore
    private var allowed = false
    private val availability = object : NotificationAvailability {
        override fun canPostMissedTargetNotifications() = allowed
    }
    private lateinit var policy: NotificationPermissionPolicy

    @Before fun setUp() {
        val file = File(folder.root, "settings.preferences_pb")
        store = NotificationPermissionStore(PreferenceDataStoreFactory.create(scope = scope) { file })
        policy = NotificationPermissionPolicy(store, availability)
    }

    @After fun tearDown() { scope.cancel() }

    @Test fun asksExactlyOnceOnApi33AndNewer() = runBlocking {
        assertTrue(policy.consumeRequestAfterCreation(sdkInt = 33))
        assertFalse("never automatically again", policy.consumeRequestAfterCreation(sdkInt = 33))
        assertTrue(store.requested.first())
    }

    @Test fun neverAsksBelowApi33AndDoesNotConsumeTheRequest() = runBlocking {
        assertFalse(policy.consumeRequestAfterCreation(sdkInt = 32))
        assertFalse("runtime permission does not exist yet", store.requested.first())
    }

    @Test fun doesNotAskWhenNotificationsAreAlreadyAllowedButStillRecordsIt() = runBlocking {
        allowed = true
        assertFalse(policy.consumeRequestAfterCreation(sdkInt = 34))
        assertTrue(store.requested.first())
        allowed = false
        assertFalse("and a later denial never re-prompts", policy.consumeRequestAfterCreation(sdkInt = 34))
    }
}
