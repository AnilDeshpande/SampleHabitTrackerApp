package com.codetutor.samplehabittrackerapp.feature.overview

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.codetutor.samplehabittrackerapp.R
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.OccurrenceResult
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.navigation.OverviewResult
import com.codetutor.samplehabittrackerapp.ui.icons.LeafIcon
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val WAIT_INDICATOR_DELAY_MS = 300L

/** Stateful entry: connects the ViewModel, the snackbar and the one-shot results from Create habit. */
@Composable
fun OverviewRoute(
    backStackEntry: NavBackStackEntry,
    onCreateHabit: () -> Unit,
    onRetiredHabits: () -> Unit,
    onRecordProgress: (Long) -> Unit,
    onReviewHabit: (Long) -> Unit,
    viewModel: OverviewViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val results = backStackEntry.savedStateHandle
    val habitSaved by results.getStateFlow(OverviewResult.KEY_HABIT_SAVED, false).collectAsStateWithLifecycle()
    val savedMessage = stringResource(R.string.overview_habit_saved)
    val scope = rememberCoroutineScope()
    // The permission result is not used: a denied permission never blocks tracking, and misses stay visible
    // in the app. The snackbar waits for the system dialog to close, because its timer would otherwise run
    // while the dialog covers it.
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        scope.launch { snackbarHostState.showSnackbar(savedMessage) }
    }

    LaunchedEffect(habitSaved) {
        if (habitSaved) {
            val askPermission = results.get<Boolean>(OverviewResult.KEY_REQUEST_NOTIFICATION_PERMISSION) == true
            // Consume both one-shot results: they are shown once and never replayed.
            results[OverviewResult.KEY_HABIT_SAVED] = false
            results[OverviewResult.KEY_REQUEST_NOTIFICATION_PERMISSION] = false
            if (askPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                snackbarHostState.showSnackbar(savedMessage)
            }
        }
    }

    OverviewScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onCreateHabit = onCreateHabit,
        onRetiredHabits = onRetiredHabits,
        onRetry = viewModel::onRetry,
        onRecordProgress = onRecordProgress,
        onReviewHabit = onReviewHabit,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    state: OverviewUiState,
    snackbarHostState: SnackbarHostState,
    onCreateHabit: () -> Unit,
    onRetiredHabits: () -> Unit,
    onRetry: () -> Unit,
    onRecordProgress: (Long) -> Unit,
    onReviewHabit: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { TopAppBar(title = { Text(stringResource(R.string.overview_title)) }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (state is OverviewUiState.Content && !state.isEmpty) {
                ExtendedFloatingActionButton(
                    onClick = onCreateHabit,
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    text = { Text(stringResource(R.string.overview_create_habit)) },
                )
            }
        },
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (state) {
                OverviewUiState.Loading -> LoadingContent()
                OverviewUiState.ReadError -> ReadErrorContent(onRetry)
                is OverviewUiState.Content ->
                    if (state.isEmpty) EmptyContent(state.points, onCreateHabit, onRetiredHabits)
                    else HabitListContent(state, onRetiredHabits, onRecordProgress, onReviewHabit)
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    // A wait indicator only if the read takes longer than 300 ms, so a fast read never flashes one.
    var showIndicator by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(WAIT_INDICATOR_DELAY_MS)
        showIndicator = true
    }
    if (showIndicator) {
        val description = stringResource(R.string.overview_loading)
        Column(
            Modifier.fillMaxSize().semantics { contentDescription = description },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.size(16.dp))
            Text(description, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun ReadErrorContent(onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            stringResource(R.string.overview_read_error),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.size(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.action_try_again)) }
    }
}

@Composable
private fun PointsCard(points: Long, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.overview_points_description, points)
    Card(
        modifier = modifier.fillMaxWidth().semantics(mergeDescendants = true) { contentDescription = description },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.overview_points_label), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(points.toString(), style = MaterialTheme.typography.headlineMedium)
        }
    }
}

/** S1: no habits. Points first, then a centred first-habit prompt that scrolls under large text sizes. */
@Composable
private fun EmptyContent(points: Long, onCreateHabit: () -> Unit, onRetiredHabits: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        PointsCard(points, Modifier.padding(top = 8.dp))
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            Column(
                Modifier.fillMaxWidth().heightIn(min = maxHeight).verticalScroll(rememberScrollState()).padding(vertical = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(72.dp)) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            LeafIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(36.dp),
                        )
                    }
                }
                Spacer(Modifier.size(24.dp))
                Text(
                    stringResource(R.string.overview_empty_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    stringResource(R.string.overview_empty_body),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.size(24.dp))
                Button(onClick = onCreateHabit) { Text(stringResource(R.string.overview_create_habit)) }
                Spacer(Modifier.size(8.dp))
                TextButton(onClick = onRetiredHabits) { Text(stringResource(R.string.overview_retired_habits)) }
            }
        }
    }
}

@Composable
private fun HabitListContent(
    state: OverviewUiState.Content,
    onRetiredHabits: () -> Unit,
    onRecordProgress: (Long) -> Unit,
    onReviewHabit: (Long) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "points") { PointsCard(state.points) }
        items(state.habits, key = { "habit-${it.habitId}" }) { habit ->
            HabitCard(habit, onRecordProgress, onReviewHabit)
        }
        items(state.pendingRetirements, key = { "pending-${it.habitId}" }) { pending ->
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
                Text(
                    stringResource(R.string.habit_pending_retirement, pending.name),
                    Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
        item(key = "retired") { TextButton(onClick = onRetiredHabits) { Text(stringResource(R.string.overview_retired_habits)) } }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HabitCard(
    habit: HabitCardUi,
    onRecordProgress: (Long) -> Unit,
    onReviewHabit: (Long) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Column(Modifier.semantics(mergeDescendants = true) {}, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    stringResource(R.string.habit_card_title, habit.name, scheduleLabel(habit, locale)),
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(stringResource(R.string.habit_target_line, habit.targetText, habit.unit), style = MaterialTheme.typography.bodyMedium)
                StatusLines(habit, locale)
                Text(stringResource(R.string.habit_run_streak, habit.activeRun, habit.successStreak), style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(if (habit.badgeEarned) R.string.habit_badge_earned else R.string.habit_badge_not_earned),
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(stringResource(R.string.habit_unacknowledged_misses, habit.unacknowledgedMisses), style = MaterialTheme.typography.bodyMedium)
                habit.lastResult?.let { last ->
                    // Success and miss are stated in words and a sign, never by colour alone.
                    val text = when {
                        last.result == OccurrenceResult.SUCCESS -> stringResource(R.string.habit_last_success, last.pointsMagnitude.toInt())
                        last.pointsMagnitude > 0 -> stringResource(R.string.habit_last_miss, last.pointsMagnitude.toInt())
                        else -> stringResource(R.string.habit_last_miss_no_points)
                    }
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (habit.canRecordProgress) {
                    val record = stringResource(R.string.overview_record_progress_for, habit.name)
                    FilledTonalButton(
                        onClick = { onRecordProgress(habit.habitId) },
                        modifier = Modifier.semantics { contentDescription = record },
                    ) { Text(stringResource(R.string.overview_record_progress)) }
                }
                val review = stringResource(R.string.overview_review_habit_for, habit.name)
                TextButton(
                    onClick = { onReviewHabit(habit.habitId) },
                    modifier = Modifier.semantics { contentDescription = review },
                ) { Text(stringResource(R.string.overview_review_habit)) }
            }
        }
    }
}

@Composable
private fun StatusLines(habit: HabitCardUi, locale: Locale) {
    val zone = remember { ZoneId.systemDefault() }
    val dateFormat = remember(locale) { DateTimeFormatter.ofPattern("EEE d MMM", locale) }
    val timeFormat = remember(locale) { DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale) }
    fun Instant.date() = atZone(zone).format(dateFormat)
    fun Instant.time() = atZone(zone).format(timeFormat)

    val style = MaterialTheme.typography.bodyMedium
    when (val status = habit.status) {
        is HabitStatusUi.InProgress -> {
            val progressRes = if (habit.frequency == Frequency.DAILY) R.string.habit_today_progress else R.string.habit_period_progress
            Text(stringResource(progressRes, status.progressText, status.targetText, habit.unit), style = style)
            if (status.targetMet) Text(stringResource(R.string.habit_target_met), style = style)
            Text(
                if (habit.frequency == Frequency.DAILY) stringResource(R.string.habit_ends_tonight, status.endsAt.time())
                else stringResource(R.string.habit_ends_at, "${status.endsAt.date()} ${status.endsAt.time()}"),
                style = style,
            )
        }
        is HabitStatusUi.WaitingForFirstPeriod -> {
            Text(stringResource(R.string.habit_waiting_first_period), style = style)
            Text(stringResource(R.string.habit_next_start, status.startsAt.date()), style = style)
        }
        is HabitStatusUi.NoOccurrenceToday -> {
            Text(stringResource(R.string.habit_no_occurrence_today), style = style)
            Text(stringResource(R.string.habit_next_start, status.nextStartsAt.date()), style = style)
        }
        is HabitStatusUi.BetweenPeriods -> {
            Text(stringResource(R.string.habit_between_periods), style = style)
            Text(stringResource(R.string.habit_next_start, "${status.nextStartsAt.date()} ${status.nextStartsAt.time()}"), style = style)
        }
    }
}

@Composable
private fun scheduleLabel(habit: HabitCardUi, locale: Locale): String = when (habit.frequency) {
    Frequency.DAILY -> stringResource(R.string.frequency_daily)
    Frequency.WEEKLY -> stringResource(R.string.frequency_weekly)
    Frequency.FORTNIGHTLY -> stringResource(R.string.frequency_fortnightly)
    Frequency.MONTHLY -> stringResource(R.string.frequency_monthly)
    Frequency.CUSTOM -> {
        val days = DayOfWeek.entries
            .filter { Weekdays.contains(habit.selectedWeekdays, it) }
            .joinToString(stringResource(R.string.weekday_separator)) { it.getDisplayName(TextStyle.SHORT, locale) }
        stringResource(R.string.schedule_custom_days, days)
    }
}
