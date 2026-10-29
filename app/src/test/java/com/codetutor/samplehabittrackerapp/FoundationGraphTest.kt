@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import androidx.hilt.work.HiltWorkerFactory
import com.codetutor.samplehabittrackerapp.core.coroutines.AppDispatchers
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import com.codetutor.samplehabittrackerapp.data.preferences.NotificationPermissionStore
import com.codetutor.samplehabittrackerapp.di.ApplicationScope
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class)
class FoundationGraphTest {
    @get:Rule
    val hilt = HiltAndroidRule(this)

    @Inject lateinit var scheduler: TestCoroutineScheduler
    @Inject lateinit var clock: Clock
    @Inject lateinit var fakeClock: FakeClock
    @Inject lateinit var dispatchers: AppDispatchers
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope
    @Inject lateinit var databaseProvider: DatabaseProvider
    @Inject lateinit var permissionStore: NotificationPermissionStore
    @Inject lateinit var workerFactory: HiltWorkerFactory

    @Before
    fun inject() = hilt.inject()

    @After
    fun tearDown() {
        appScope.coroutineContext[kotlinx.coroutines.Job]?.cancel()
        databaseProvider.close()
    }

    @Test
    fun testGraphReplacesProductionTimeAndConcurrency() {
        assertSame(fakeClock, clock)
        assertNotNull(dispatchers.io)
        assertTrue(appScope.coroutineContext[kotlinx.coroutines.Job] != null)
    }

    @Test
    fun databaseIsSeededInMemoryThroughTheProvider() = runTest(scheduler) {
        DatabaseSmoke.verifySeeded(databaseProvider)
    }

    @Test
    fun permissionFlagDefaultsToFalseAndSticksOnceMarked() = runTest(scheduler) {
        assertFalse(permissionStore.requested.first())
        permissionStore.markRequested()
        assertTrue(permissionStore.requested.first())
    }
}
