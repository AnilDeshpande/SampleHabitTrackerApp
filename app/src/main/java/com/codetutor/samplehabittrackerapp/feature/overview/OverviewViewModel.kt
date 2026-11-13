package com.codetutor.samplehabittrackerapp.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codetutor.samplehabittrackerapp.core.lifecycle.ForegroundCoordinator
import com.codetutor.samplehabittrackerapp.core.ui.DecimalFormatter
import com.codetutor.samplehabittrackerapp.domain.repository.HabitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Observes the durable overview. A failed read becomes [OverviewUiState.ReadError], never an empty
 * state, and [retry] reads again. Opening the overview asks the one foreground pipeline for catch-up.
 */
@HiltViewModel
class OverviewViewModel @Inject constructor(
    repository: HabitRepository,
    coordinator: ForegroundCoordinator,
    private val formatter: DecimalFormatter,
) : ViewModel() {

    private val reads = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<OverviewUiState> = reads
        .flatMapLatest {
            flow {
                emit(OverviewUiState.Loading)
                emitAll(
                    repository.observeOverview()
                        .map<_, OverviewUiState> { it.toUiState(formatter, Locale.getDefault()) }
                        .catch { failure ->
                            if (failure is CancellationException) throw failure
                            emit(OverviewUiState.ReadError)
                        },
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), OverviewUiState.Loading)

    init {
        coordinator.requestCatchUp()
    }

    fun onRetry() {
        reads.tryEmit(Unit)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
