package com.codetutor.samplehabittrackerapp

import android.os.Build
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

/**
 * The shared repository and guard scenarios on a file-backed WAL database with the platform SQLite.
 * Passing on API 24 (SQLite 3.9) is the minimum-API evidence the LLD requires; every test logs
 * the API level and sqlite_version() under the SqlEvidence tag.
 */
@RunWith(AndroidJUnit4::class)
class RepositoryDeviceTest {
    @get:Rule val testName = TestName()
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var fileName: String
    private lateinit var fx: RepositoryFixture

    @Before fun setUp() {
        fileName = "device-${testName.methodName}.db"
        context.deleteDatabase(fileName)
        fx = RepositoryFixture(context, fileName)
    }

    @After fun tearDown() {
        fx.close()
        context.deleteDatabase(fileName)
    }

    private fun record() = runBlocking {
        val sql = fx.provider.get().openHelper.writableDatabase
        val version = sql.query("SELECT sqlite_version()").use { it.moveToFirst(); it.getString(0) }
        val journal = sql.query("PRAGMA journal_mode").use { it.moveToFirst(); it.getString(0) }
        Log.i("SqlEvidence", "${testName.methodName}: api=${Build.VERSION.SDK_INT} sqlite=$version journal_mode=$journal")
    }

    private fun run(scenario: suspend (RepositoryFixture) -> Unit) {
        runBlocking { scenario(fx) }
        record()
    }

    @Test fun createsDailyHabitAndOpensItsFirstPeriod() = run(RepositoryScenarios::createsDailyHabitAndOpensItsFirstPeriod)
    @Test fun reconcileIsIdempotent() = run(RepositoryScenarios::reconcileIsIdempotent)
    @Test fun rejectsInvalidAndDuplicateInputWithoutSaving() = run(RepositoryScenarios::rejectsInvalidAndDuplicateInputWithoutSaving)
    @Test fun weeklyCreatedOnSundayWaitsThenOpensOnMonday() = run(RepositoryScenarios::weeklyCreatedOnSundayWaitsThenOpensOnMonday)
    @Test fun customHabitShowsNoOccurrenceOnAnUnselectedDay() = run(RepositoryScenarios::customHabitShowsNoOccurrenceOnAnUnselectedDay)
    @Test fun missFinalizesWithAnOutboxEventAndNoPointsAtZero() = run(RepositoryScenarios::missFinalizesWithAnOutboxEventAndNoPointsAtZero)
    @Test fun successFinalizesWithTenPoints() = run(RepositoryScenarios::successFinalizesWithTenPoints)
    @Test fun consecutiveMissesDeductRoundedUpPercentOfTheBalance() = run(RepositoryScenarios::consecutiveMissesDeductRoundedUpPercentOfTheBalance)
    @Test fun catchUpAfterALongAbsenceFinalizesEveryElapsedPeriodOnce() = run(RepositoryScenarios::catchUpAfterALongAbsenceFinalizesEveryElapsedPeriodOnce)
    @Test fun gateIncrementsOncePerGatedTransactionAndRollsBackWithIt() = run(RepositoryScenarios::gateIncrementsOncePerGatedTransactionAndRollsBackWithIt)
    @Test fun badgeIsAwardedOnceAfterEighteenSuccessfulDays() = run(RepositoryScenarios::badgeIsAwardedOnceAfterEighteenSuccessfulDays)
    @Test fun nextBoundaryFollowsPeriodEndsStartsAndDeadlines() = run(RepositoryScenarios::nextBoundaryFollowsPeriodEndsStartsAndDeadlines)
    @Test fun expiredRetirementCancelsTheFrozenPeriodWithoutScoring() = run(RepositoryScenarios::expiredRetirementCancelsTheFrozenPeriodWithoutScoring)
    @Test fun overviewReEmitsAfterACommittedChange() = run(RepositoryScenarios::overviewReEmitsAfterACommittedChange)

    @Test fun everyGuardTriggerExistsAfterOpen() = run(GuardScenarios::everyGuardTriggerExistsAfterOpen)
    @Test fun habitGuards() = run(GuardScenarios::habitGuards)
    @Test fun occurrenceGuards() = run(GuardScenarios::occurrenceGuards)
    @Test fun balanceAndForeignKeyGuards() = run(GuardScenarios::balanceAndForeignKeyGuards)
}
