package com.codetutor.samplehabittrackerapp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import java.time.Duration
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Two independent Room instances on one WAL file, each with its own query and transaction executor,
 * run catch-up from real concurrent threads. In-memory tests cannot prove the gate: the database must
 * serialize the contenders so each elapsed period is finalized exactly once.
 */
@RunWith(AndroidJUnit4::class)
class ConcurrentGateDeviceTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val fileName = "device-concurrent-gate.db"
    private val clock = ManualClock()
    private lateinit var first: RepositoryFixture
    private lateinit var second: RepositoryFixture

    @Before fun setUp() {
        context.deleteDatabase(fileName)
        first = RepositoryFixture(context, fileName, clock)
        runBlocking { first.provider.get() } // create and migrate once before the second instance opens it
        second = RepositoryFixture(context, fileName, clock)
    }

    @After fun tearDown() {
        first.close()
        second.close()
        context.deleteDatabase(fileName)
    }

    @Test
    fun concurrentCatchUpFinalizesEachElapsedPeriodExactlyOnce() {
        runBlocking {
            first.repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), Locale.US)
        }
        clock.instant = clock.instant.plus(Duration.ofDays(5)).plusSeconds(3600)

        val workers = 8
        val pool = Executors.newFixedThreadPool(workers)
        val ready = CountDownLatch(workers)
        val go = CountDownLatch(1)
        val finalizedCounts = java.util.Collections.synchronizedList(mutableListOf<Int>())
        val failures = java.util.Collections.synchronizedList(mutableListOf<Throwable>())
        val done = CountDownLatch(workers)
        repeat(workers) { index ->
            pool.execute {
                try {
                    val repository = if (index % 2 == 0) first.repository else second.repository
                    ready.countDown()
                    go.await()
                    finalizedCounts += runBlocking { repository.reconcile() }.finalizedCount
                } catch (t: Throwable) {
                    failures += t
                } finally {
                    done.countDown()
                }
            }
        }
        ready.await()
        go.countDown()
        assertTrue("workers did not finish", done.await(60, TimeUnit.SECONDS))
        pool.shutdownNow()

        assertEquals("no contender should fail: $failures", emptyList<Throwable>(), failures)
        assertEquals(5, finalizedCounts.sum()) // each of the five elapsed periods, exactly once
        runBlocking {
            assertEquals(5L, first.count("SELECT COUNT(*) FROM occurrences WHERE status = 'FINALIZED'"))
            assertEquals(6L, first.count("SELECT COUNT(*) FROM occurrences"))
            assertEquals(5L, first.count("SELECT COUNT(*) FROM notification_events"))
            assertEquals(0L, first.count("SELECT pointsBalance FROM app_state"))
            // One gated transaction for the create plus one per reconcile call: the gate ran every time.
            assertEquals(1L + workers, first.count("SELECT version FROM reconciliation_lock"))
        }
    }
}
