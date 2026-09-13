package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.feature.settings.components.ReminderTimePicker
import com.leeseungyun1020.manicule.feature.settings.components.ReminderToggle
import kotlinx.datetime.LocalTime
import java.util.Calendar
import android.text.format.DateFormat as AndroidDateFormat

internal const val SETTINGS_CONTENT_TEST_TAG = "settings_content"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    snackbarHostState: SnackbarHostState,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (LocalTime) -> Unit,
    onRetryPreferences: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ManiculeTopAppBar(
                title = stringResource(R.string.settings_title),
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { ManiculeSnackbarHost(hostState = snackbarHostState) },
    ) { contentPadding ->
        when (uiState) {
            SettingsUiState.Loading -> SettingsLoading(Modifier.padding(contentPadding))
            SettingsUiState.Error ->
                SettingsError(
                    onRetry = onRetryPreferences,
                    modifier = Modifier.padding(contentPadding),
                )

            is SettingsUiState.Content ->
                SettingsContent(
                    state = uiState,
                    onReminderEnabledChange = onReminderEnabledChange,
                    onReminderTimeChange = onReminderTimeChange,
                    modifier = Modifier.padding(contentPadding),
                )
        }
    }
}

@Composable
private fun SettingsLoading(modifier: Modifier = Modifier) {
    val description = stringResource(R.string.settings_loading)
    ManiculeLoading(
        modifier =
            modifier
                .fillMaxSize()
                .semantics { contentDescription = description },
    )
}

@Composable
private fun SettingsError(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(MaterialTheme.spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        ManiculeEmptyState(
            title = stringResource(R.string.settings_load_error_title),
            description = stringResource(R.string.settings_load_error_description),
            modifier = Modifier.widthIn(max = MaterialTheme.size.contentMaxWidth),
            actions = {
                ManiculeButton(
                    onClick = onRetry,
                    text = stringResource(R.string.settings_retry),
                )
            },
        )
    }
}

@Composable
private fun SettingsContent(
    state: SettingsUiState.Content,
    onReminderEnabledChange: (Boolean) -> Unit,
    onReminderTimeChange: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val is24Hour = AndroidDateFormat.is24HourFormat(context)
    val formattedTime =
        remember(state.reminder.time, configuration, is24Hour) {
            val calendar =
                Calendar.getInstance().apply {
                    clear()
                    set(Calendar.HOUR_OF_DAY, state.reminder.time.hour)
                    set(Calendar.MINUTE, state.reminder.time.minute)
                }
            AndroidDateFormat.getTimeFormat(context).format(calendar.time)
        }

    LaunchedEffect(state.reminder.enabled) {
        if (!state.reminder.enabled) showTimePicker = false
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier =
                Modifier
                    .widthIn(max = MaterialTheme.size.contentMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.screenContent)
                    .testTag(SETTINGS_CONTENT_TEST_TAG),
        ) {
            ReminderToggle(
                reminder = state.reminder,
                formattedTime = formattedTime,
                enabled = !state.isUpdating,
                onEnabledChange = onReminderEnabledChange,
                onTimeClick = { showTimePicker = true },
            )
        }
    }

    if (showTimePicker) {
        ReminderTimePicker(
            initialTime = state.reminder.time,
            is24Hour = is24Hour,
            onDismiss = { showTimePicker = false },
            onConfirm = { selectedTime ->
                showTimePicker = false
                onReminderTimeChange(selectedTime)
            },
        )
    }
}

@ManiculePreview
@Composable
private fun SettingsLoadingPreview() {
    ManiculePreviewTheme {
        SettingsScreen(
            uiState = SettingsUiState.Loading,
            snackbarHostState = SnackbarHostState(),
            onReminderEnabledChange = {},
            onReminderTimeChange = {},
            onRetryPreferences = {},
        )
    }
}

@ManiculePreview
@Composable
private fun SettingsErrorPreview() {
    ManiculePreviewTheme {
        SettingsScreen(
            uiState = SettingsUiState.Error,
            snackbarHostState = SnackbarHostState(),
            onReminderEnabledChange = {},
            onReminderTimeChange = {},
            onRetryPreferences = {},
        )
    }
}

@ManiculePreview
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun SettingsScreenPreview() {
    ManiculePreviewTheme {
        SettingsScreen(
            uiState = SettingsUiState.Content(ReminderConfig(enabled = true, time = LocalTime(21, 0))),
            snackbarHostState = SnackbarHostState(),
            onReminderEnabledChange = {},
            onReminderTimeChange = {},
            onRetryPreferences = {},
        )
    }
}

@ManiculePreview
@Composable
private fun DisabledReminderSettingsScreenPreview() {
    ManiculePreviewTheme {
        SettingsScreen(
            uiState = SettingsUiState.Content(ReminderConfig.Default),
            snackbarHostState = SnackbarHostState(),
            onReminderEnabledChange = {},
            onReminderTimeChange = {},
            onRetryPreferences = {},
        )
    }
}

@ManiculePreview
@Composable
private fun UpdatingReminderSettingsScreenPreview() {
    ManiculePreviewTheme {
        SettingsScreen(
            uiState =
                SettingsUiState.Content(
                    reminder = ReminderConfig(enabled = true, time = LocalTime(21, 0)),
                    isUpdating = true,
                ),
            snackbarHostState = SnackbarHostState(),
            onReminderEnabledChange = {},
            onReminderTimeChange = {},
            onRetryPreferences = {},
        )
    }
}
