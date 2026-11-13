package com.codetutor.samplehabittrackerapp.feature.habitform

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codetutor.samplehabittrackerapp.R
import com.codetutor.samplehabittrackerapp.domain.model.Frequency
import com.codetutor.samplehabittrackerapp.domain.model.Weekdays
import com.codetutor.samplehabittrackerapp.domain.rules.DecimalError
import com.codetutor.samplehabittrackerapp.domain.rules.UnitError
import com.codetutor.samplehabittrackerapp.domain.rules.WeekdaysError
import java.time.DayOfWeek
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.delay

private const val WAIT_INDICATOR_DELAY_MS = 300L

/** Stateful entry: connects the ViewModel and turns its one-off effects into navigation. */
@Composable
fun HabitFormRoute(
    onClose: () -> Unit,
    onSaved: (requestNotificationPermission: Boolean) -> Unit,
    viewModel: HabitFormViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnClose by rememberUpdatedState(onClose)
    val currentOnSaved by rememberUpdatedState(onSaved)
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HabitFormEffect.Saved -> currentOnSaved(effect.requestNotificationPermission)
                HabitFormEffect.NavigateBack -> currentOnClose()
            }
        }
    }
    HabitFormScreen(
        state = state,
        onNameChange = viewModel::onNameChange,
        onUnitChange = viewModel::onUnitChange,
        onTargetChange = viewModel::onTargetChange,
        onFrequencyChange = viewModel::onFrequencyChange,
        onWeekdayToggle = viewModel::onWeekdayToggle,
        onSave = viewModel::onSave,
        onBack = viewModel::onBack,
        onKeepEditing = viewModel::onKeepEditing,
        onDiscard = viewModel::onDiscard,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormScreen(
    state: HabitFormUiState,
    onNameChange: (String) -> Unit,
    onUnitChange: (String) -> Unit,
    onTargetChange: (String) -> Unit,
    onFrequencyChange: (Frequency) -> Unit,
    onWeekdayToggle: (DayOfWeek) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    onKeepEditing: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Back and Up both ask the ViewModel, which decides between the discard dialog and leaving.
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.form_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val focus = LocalFocusManager.current
            FormTextField(
                text = state.name,
                onTextChange = onNameChange,
                label = stringResource(R.string.form_name_label),
                error = state.nameError?.let { nameErrorText(it) },
                helper = stringResource(R.string.form_name_helper),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            )
            FormTextField(
                text = state.unit,
                onTextChange = onUnitChange,
                label = stringResource(R.string.form_unit_label),
                error = state.unitError?.let { unitErrorText(it) },
                helper = stringResource(R.string.form_unit_helper),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focus.moveFocus(FocusDirection.Down) }),
            )
            FormTextField(
                text = state.target,
                onTextChange = onTargetChange,
                label = stringResource(R.string.form_target_label),
                error = state.targetError?.let { targetErrorText(it) },
                helper = stringResource(R.string.form_target_helper),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            )

            FrequencySection(state, onFrequencyChange, onWeekdayToggle)

            if (state.submission == SubmissionState.Failed) {
                Text(
                    stringResource(R.string.form_save_failed),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                )
            }
            Spacer(Modifier.size(8.dp))
            SaveButton(state, onSave)
            TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.form_cancel)) }
        }
    }

    if (state.showDiscardDialog) {
        AlertDialog(
            onDismissRequest = onKeepEditing,
            title = { Text(stringResource(R.string.form_discard_title)) },
            text = { Text(stringResource(R.string.form_discard_body)) },
            // The dismissive action first, as the component mapping asks.
            dismissButton = { TextButton(onClick = onKeepEditing) { Text(stringResource(R.string.form_keep_editing)) } },
            confirmButton = { TextButton(onClick = onDiscard) { Text(stringResource(R.string.form_discard)) } },
        )
    }
}

/**
 * The ViewModel owns the text; the field keeps its own cursor and selection (visual-only state) and
 * adopts the ViewModel's text whenever it differs, so typing never drops characters or jumps the cursor.
 */
@Composable
private fun FormTextField(
    text: String,
    onTextChange: (String) -> Unit,
    label: String,
    error: String?,
    helper: String,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
) {
    var field by remember { mutableStateOf(TextFieldValue(text, TextRange(text.length))) }
    if (field.text != text) field = TextFieldValue(text, TextRange(text.length))
    OutlinedTextField(
        value = field,
        onValueChange = {
            field = it
            if (it.text != text) onTextChange(it.text)
        },
        label = { Text(label) },
        isError = error != null,
        supportingText = { Text(error ?: helper) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        modifier = Modifier.fillMaxWidth(),
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FrequencySection(
    state: HabitFormUiState,
    onFrequencyChange: (Frequency) -> Unit,
    onWeekdayToggle: (DayOfWeek) -> Unit,
) {
    val locale = LocalConfiguration.current.locales[0]
    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(Modifier.padding(vertical = 8.dp)) {
            Text(
                stringResource(R.string.form_frequency_label),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Column(Modifier.selectableGroup()) {
                Frequency.entries.forEach { frequency ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .selectable(
                                selected = state.frequency == frequency,
                                onClick = { onFrequencyChange(frequency) },
                                role = Role.RadioButton,
                            )
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = state.frequency == frequency, onClick = null)
                        Text(frequencyLabel(frequency), Modifier.padding(start = 16.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
            if (state.frequency == Frequency.CUSTOM) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Text(stringResource(R.string.form_weekdays_label), style = MaterialTheme.typography.labelLarge)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DayOfWeek.entries.forEach { day ->
                            val selected = Weekdays.contains(state.weekdays, day)
                            FilterChip(
                                selected = selected,
                                onClick = { onWeekdayToggle(day) },
                                label = { Text(day.getDisplayName(TextStyle.SHORT, locale)) },
                                // A check mark as well as the fill, so selection never relies on colour alone.
                                leadingIcon = if (selected) {
                                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(FilterChipDefaults.IconSize)) }
                                } else {
                                    null
                                },
                            )
                        }
                    }
                    state.weekdaysError?.let {
                        Text(
                            weekdaysErrorText(it),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            }
            state.startsOn?.let {
                Text(
                    startsOnText(it, locale),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }
    }
}

@Composable
private fun SaveButton(state: HabitFormUiState, onSave: () -> Unit) {
    // A wait indicator only if saving takes longer than 300 ms. Only this Save is disabled while it runs.
    var showWait by remember { mutableStateOf(false) }
    LaunchedEffect(state.isSaving) {
        showWait = false
        if (state.isSaving) {
            delay(WAIT_INDICATOR_DELAY_MS)
            showWait = true
        }
    }
    Button(onClick = onSave, enabled = !state.isSaving, modifier = Modifier.fillMaxWidth()) {
        if (state.isSaving && showWait) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.size(8.dp))
        }
        Text(
            stringResource(
                when (state.submission) {
                    SubmissionState.Saving -> R.string.form_saving
                    SubmissionState.Failed -> R.string.form_retry
                    SubmissionState.Idle -> R.string.form_save
                },
            ),
        )
    }
}

@Composable
private fun frequencyLabel(frequency: Frequency): String = stringResource(
    when (frequency) {
        Frequency.DAILY -> R.string.frequency_daily
        Frequency.WEEKLY -> R.string.frequency_weekly
        Frequency.FORTNIGHTLY -> R.string.frequency_fortnightly
        Frequency.MONTHLY -> R.string.frequency_monthly
        Frequency.CUSTOM -> R.string.frequency_custom
    },
)

@Composable
private fun startsOnText(preview: StartPreview, locale: Locale): String = when (preview) {
    StartPreview.Today -> stringResource(R.string.form_starts_today)
    is StartPreview.On -> stringResource(R.string.form_starts_on, preview.date.format(DateTimeFormatter.ofPattern("EEE d MMM", locale)))
}

@Composable
private fun nameErrorText(error: NameFieldError): String = when (error) {
    NameFieldError.Empty -> stringResource(R.string.error_name_empty)
    NameFieldError.TooLong -> stringResource(R.string.error_name_too_long)
    is NameFieldError.Duplicate -> stringResource(R.string.error_name_duplicate, error.name)
}

@Composable
private fun unitErrorText(error: UnitError): String = stringResource(
    when (error) {
        UnitError.EMPTY -> R.string.error_unit_empty
        UnitError.TOO_LONG -> R.string.error_unit_too_long
    },
)

@Composable
private fun targetErrorText(error: DecimalError): String = stringResource(
    when (error) {
        DecimalError.EMPTY -> R.string.error_target_empty
        DecimalError.NOT_A_NUMBER -> R.string.error_target_not_a_number
        DecimalError.TOO_MANY_INTEGER_DIGITS -> R.string.error_target_integer_digits
        DecimalError.TOO_MANY_FRACTION_DIGITS -> R.string.error_target_fraction_digits
        DecimalError.MUST_BE_POSITIVE -> R.string.error_target_positive
    },
)

@Composable
private fun weekdaysErrorText(error: WeekdaysError): String = stringResource(
    when (error) {
        WeekdaysError.NONE_SELECTED, WeekdaysError.OUT_OF_RANGE -> R.string.error_weekdays_none
    },
)
