package com.codetutor.samplehabittrackerapp

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Real Room (in memory) + real rules + real repository; only time is controlled. Host SQLite is a
 * development aid, not minimum-API evidence: the same scenarios run on API 24 in the device tier.
 */
@RunWith(RobolectricTestRunner::class)
class RoomHabitRepositoryTest {
    private lateinit var fx: RepositoryFixture

    @Before fun setUp() { fx = RepositoryFixture(ApplicationProvider.getApplicationContext()) }
    @After fun tearDown() { fx.close() }

    @Test fun createsDailyHabitAndOpensItsFirstPeriod() = runBlocking { RepositoryScenarios.createsDailyHabitAndOpensItsFirstPeriod(fx) }
    @Test fun reconcileIsIdempotent() = runBlocking { RepositoryScenarios.reconcileIsIdempotent(fx) }
    @Test fun rejectsInvalidAndDuplicateInputWithoutSaving() = runBlocking { RepositoryScenarios.rejectsInvalidAndDuplicateInputWithoutSaving(fx) }
    @Test fun weeklyCreatedOnSundayWaitsThenOpensOnMonday() = runBlocking { RepositoryScenarios.weeklyCreatedOnSundayWaitsThenOpensOnMonday(fx) }
    @Test fun customHabitShowsNoOccurrenceOnAnUnselectedDay() = runBlocking { RepositoryScenarios.customHabitShowsNoOccurrenceOnAnUnselectedDay(fx) }
    @Test fun missFinalizesWithAnOutboxEventAndNoPointsAtZero() = runBlocking { RepositoryScenarios.missFinalizesWithAnOutboxEventAndNoPointsAtZero(fx) }
    @Test fun successFinalizesWithTenPoints() = runBlocking { RepositoryScenarios.successFinalizesWithTenPoints(fx) }
    @Test fun consecutiveMissesDeductRoundedUpPercentOfTheBalance() = runBlocking { RepositoryScenarios.consecutiveMissesDeductRoundedUpPercentOfTheBalance(fx) }
    @Test fun catchUpAfterALongAbsenceFinalizesEveryElapsedPeriodOnce() = runBlocking { RepositoryScenarios.catchUpAfterALongAbsenceFinalizesEveryElapsedPeriodOnce(fx) }
    @Test fun gateIncrementsOncePerGatedTransactionAndRollsBackWithIt() = runBlocking { RepositoryScenarios.gateIncrementsOncePerGatedTransactionAndRollsBackWithIt(fx) }
    @Test fun badgeIsAwardedOnceAfterEighteenSuccessfulDays() = runBlocking { RepositoryScenarios.badgeIsAwardedOnceAfterEighteenSuccessfulDays(fx) }
    @Test fun nextBoundaryFollowsPeriodEndsStartsAndDeadlines() = runBlocking { RepositoryScenarios.nextBoundaryFollowsPeriodEndsStartsAndDeadlines(fx) }
    @Test fun expiredRetirementCancelsTheFrozenPeriodWithoutScoring() = runBlocking { RepositoryScenarios.expiredRetirementCancelsTheFrozenPeriodWithoutScoring(fx) }
    @Test fun overviewReEmitsAfterACommittedChange() = runBlocking { RepositoryScenarios.overviewReEmitsAfterACommittedChange(fx) }
}
