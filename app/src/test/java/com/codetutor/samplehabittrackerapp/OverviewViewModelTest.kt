@file:OptIn(ExperimentalCoroutinesApi::class)

package com.codetutor.samplehabittrackerapp

import androidx.test.core.app.ApplicationProvider
import com.codetutor.samplehabittrackerapp.core.lifecycle.ForegroundCoordinator
import com.codetutor.samplehabittrackerapp.core.time.BoundaryTicker
import com.codetutor.samplehabittrackerapp.core.time.TimeChangeSource
import com.codetutor.samplehabittrackerapp.core.ui.DecimalFormatter
import com.codetutor.samplehabittrackerapp.core.work.TrackingCatchUp
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OverviewData
import com.codetutor.samplehabittrackerapp.domain.repository.CreateHabitResult
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import com.codetutor.samplehabittrackerapp.domain.repository.ReconcileSummary
import com.codetutor.samplehabittrackerapp.domain.rules.RawHabitInput
import com.codetutor.samplehabittrackerapp.feature.overview.HabitStatusUi
import com.codetutor.samplehabittrackerapp.feature.overview.OverviewUiState
import com.codetutor.samplehabittrackerapp.feature.overview.OverviewViewModel
import java.io.IOException
import java.time.Instant
import java.util.Locale
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/** Overview state mapping against the real repository, plus the read-failure path with a failing repository. */
@RunWith(RobolectricTestRunner::class)
class OverviewViewModelTest {
    private lateinit var fx: RepositoryFixture
    private lateinit var appScope: CoroutineScope
    private lateinit var uiScope: CoroutineScope
    private val previousLocale = Locale.getDefault()

    @Before fun setUp() {
        Locale.setDefault(Locale.US)
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fx = RepositoryFixture(ApplicationProvider.getApplicationContext())
        appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        uiScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    }

    @After fun tearDown() {
        uiScope.cancel(); appScope.cancel(); fx.close()
        Dispatchers.resetMain(); Locale.setDefault(previousLocale)
    }

    private val noTimeChanges = object : TimeChangeSource { override val events: Flow<Unit> = emptyFlow() }

    private fun coordinator(repository: HabitRepository): ForegroundCoordinator {
        val catchUp = TrackingCatchUp(repository)
        return ForegroundCoordinator(appScope, catchUp, BoundaryTicker(appScope, fx.clock, repository, catchUp, noTimeChanges))
    }

    private fun viewModel(repository: HabitRepository = fx.repository) =
        OverviewViewModel(repository, coordinator(repository), DecimalFormatter())

    private fun OverviewViewModel.observe(): () -> OverviewUiState {
        uiScope.launch { uiState.collect { } }
        return { uiState.value }
    }

    private suspend fun awaitState(state: () -> OverviewUiState, predicate: (OverviewUiState) -> Boolean): OverviewUiState =
        withTimeout(15_000) { while (!predicate(state())) delay(10); state() }

    @Test fun aFreshDatabaseIsTheEmptyStateNotAnError() = runBlocking {
        val state = viewModel().observe()
        val loaded = awaitState(state) { it is OverviewUiState.Content }
        loaded as OverviewUiState.Content
        assertTrue(loaded.isEmpty)
        assertEquals(0L, loaded.points)
    }

    @Test fun aCreatedHabitAppearsAsAnInProgressCard() = runBlocking {
        val state = viewModel().observe()
        awaitState(state) { it is OverviewUiState.Content }
        fx.repository.createHabit(RawHabitInput("Meditation", "minutes", "10", Frequency.DAILY, 0), Locale.US)
        val content = awaitState(state) { it is OverviewUiState.Content && !it.isEmpty } as OverviewUiState.Content
        val card = content.habits.single()
        assertEquals("Meditation", card.name)
        assertEquals("10", card.targetText)
        assertEquals(1, card.activeRun)
        assertTrue(card.canRecordProgress)
        val status = card.status as HabitStatusUi.InProgress
        assertEquals("0", status.progressText)
        assertEquals(false, status.targetMet)
        assertEquals(Instant.parse("2026-10-12T00:00:00Z"), status.endsAt)
    }

    @Test fun aWaitingHabitOffersNoRecordProgress() = runBlocking {
        val state = viewModel().observe()
        fx.repository.createHabit(RawHabitInput("Reading", "pages", "20", Frequency.WEEKLY, 0), Locale.US)
        val content = awaitState(state) { it is OverviewUiState.Content && !it.isEmpty } as OverviewUiState.Content
        val card = content.habits.single()
        assertTrue(card.status is HabitStatusUi.WaitingForFirstPeriod)
        assertEquals(false, card.canRecordProgress)
    }

    @Test fun targetsUseTheDeviceLocaleSeparator() = runBlocking {
        Locale.setDefault(Locale.GERMANY)
        val state = viewModel().observe()
        fx.repository.createHabit(RawHabitInput("Lesen", "Seiten", "2,5", Frequency.DAILY, 0), Locale.GERMANY)
        val content = awaitState(state) { it is OverviewUiState.Content && !it.isEmpty } as OverviewUiState.Content
        assertEquals("2,5", content.habits.single().targetText)
    }

    @Test fun overviewOpenRequestsCatchUpThroughTheForegroundPipeline() = runBlocking {
        val reconciles = AtomicInteger()
        val counting = object : HabitRepository by fx.repository {
            override suspend fun reconcile(): ReconcileSummary { reconciles.incrementAndGet(); return fx.repository.reconcile() }
        }
        viewModel(counting).observe()
        withTimeout(15_000) { while (reconciles.get() == 0) delay(10) }
        assertTrue(reconciles.get() >= 1)
    }

    @Test fun aFailedReadIsAnErrorNeverAnEmptyStateAndRetryRecovers() = runBlocking {
        var failing = true
        val unreliable = object : HabitRepository by fx.repository {
            override fun observeOverview(): Flow<OverviewData> =
                if (failing) flow { throw IOException("local read failed") } else fx.repository.observeOverview()
        }
        val vm = viewModel(unreliable); val state = vm.observe()
        awaitState(state) { it == OverviewUiState.ReadError }
        assertTrue("never a false empty state", state() !is OverviewUiState.Content)

        failing = false
        vm.onRetry()
        val recovered = awaitState(state) { it is OverviewUiState.Content } as OverviewUiState.Content
        assertTrue(recovered.isEmpty)
    }

    @Test fun startsInLoadingBeforeTheFirstRead() {
        val vm = viewModel()
        assertEquals(OverviewUiState.Loading, vm.uiState.value)
    }

    @Test fun createHabitResultTypeIsStable() {
        assertEquals(CreateHabitResult.DuplicateName, CreateHabitResult.DuplicateName)
    }
}
