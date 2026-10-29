@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.codetutor.samplehabittrackerapp.core.time.Clock
import com.codetutor.samplehabittrackerapp.data.local.DatabaseProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HiltGraphDeviceTest {
    @get:Rule
    val hilt = HiltAndroidRule(this)

    @Inject lateinit var scheduler: TestCoroutineScheduler
    @Inject lateinit var clock: Clock
    @Inject lateinit var fakeClock: FakeClock
    @Inject lateinit var databaseProvider: DatabaseProvider

    @Before
    fun inject() = hilt.inject()

    @After
    fun tearDown() = databaseProvider.close()

    @Test
    fun testApplicationBuildsTheTestGraphOnDevice() = runTest(scheduler) {
        assertSame(fakeClock, clock)
        DatabaseSmoke.verifySeeded(databaseProvider)
    }
}
