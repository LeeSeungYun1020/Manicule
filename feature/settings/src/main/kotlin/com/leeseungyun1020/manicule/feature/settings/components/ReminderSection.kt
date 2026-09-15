package com.leeseungyun1020.manicule.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextButton
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.feature.settings.R
import com.leeseungyun1020.manicule.feature.settings.ReminderUiState
import com.leeseungyun1020.manicule.feature.settings.displayedReminder
import kotlinx.datetime.LocalTime
import java.util.Calendar
import android.text.format.DateFormat as AndroidDateFormat

@Composable
internal fun ReminderSection(
    state: ReminderUiState,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (LocalTime) -> Unit,
    onRetry: () -> Unit,
) {
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    val reminder = state.displayedReminder
    val canEdit = state is ReminderUiState.Content && !state.isUpdating
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val is24Hour = AndroidDateFormat.is24HourFormat(context)
    val formattedTime = remember(reminder?.time, configuration, is24Hour) {
        reminder?.time?.let { time ->
            val calendar = Calendar.getInstance().apply {
                clear()
                set(Calendar.HOUR_OF_DAY, time.hour)
                set(Calendar.MINUTE, time.minute)
            }
            AndroidDateFormat.getTimeFormat(context).format(calendar.time)
        }.orEmpty()
    }

    LaunchedEffect(canEdit, reminder?.enabled) {
        if (!canEdit || reminder?.enabled != true) showTimePicker = false
    }

    Column {
        ManiculeSectionHeader(title = stringResource(R.string.settings_notifications_section))
        if (reminder != null) {
            ReminderToggle(
                reminder = reminder,
                formattedTime = formattedTime,
                enabled = canEdit,
                onEnabledChange = onEnabledChange,
                onTimeClick = { showTimePicker = true },
            )
        } else {
            ListItem(headlineContent = { Text(stringResource(R.string.settings_reading_reminder)) })
        }
        ReminderStatus(state, onRetry)
    }

    if (showTimePicker && canEdit && reminder?.enabled == true) {
        ReminderTimePicker(
            initialTime = reminder.time,
            is24Hour = is24Hour,
            onDismiss = { showTimePicker = false },
            onConfirm = { time ->
                showTimePicker = false
                onTimeChange(time)
            },
        )
    }
}

@Composable
private fun ReminderStatus(
    state: ReminderUiState,
    onRetry: () -> Unit,
) {
    when (state) {
        is ReminderUiState.Loading -> {
            val description = stringResource(R.string.settings_loading)
            Row(
                modifier = Modifier.padding(MaterialTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ManiculeLoading(
                    modifier = Modifier.size(MaterialTheme.size.touchTargetMin)
                        .semantics { contentDescription = description },
                )
                Text(description, style = MaterialTheme.typography.bodyMedium)
            }
        }
        is ReminderUiState.Error -> {
            Row(
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    Text(
                        text = stringResource(R.string.settings_load_error_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (state.previous != null) {
                        Text(
                            text = stringResource(R.string.settings_previous_reminder_description),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                ManiculeTextButton(onClick = onRetry, text = stringResource(R.string.settings_retry))
            }
        }
        is ReminderUiState.Content -> Unit
    }
}

internal class ReminderUiStatePreviewProvider : PreviewParameterProvider<ReminderUiState> {
    override val values: Sequence<ReminderUiState> = sequenceOf(
        ReminderUiState.Loading(),
        ReminderUiState.Loading(ReminderConfig.Default),
        ReminderUiState.Loading(ReminderConfig(true, LocalTime(8, 30))),
        ReminderUiState.Error(),
        ReminderUiState.Error(ReminderConfig.Default),
        ReminderUiState.Error(ReminderConfig(true, LocalTime(8, 30))),
        ReminderUiState.Content(ReminderConfig.Default),
        ReminderUiState.Content(ReminderConfig(true, LocalTime(8, 30))),
        ReminderUiState.Content(ReminderConfig.Default, isUpdating = true),
        ReminderUiState.Content(ReminderConfig(true, LocalTime(8, 30)), isUpdating = true),
    )
}

@ManiculePreview
@Composable
private fun ReminderSectionPreview(
    @PreviewParameter(ReminderUiStatePreviewProvider::class) state: ReminderUiState,
) {
    ManiculePreviewTheme {
        ReminderSection(state, onEnabledChange = {}, onTimeChange = {}, onRetry = {})
    }
}

@ManiculePreview
@Composable
private fun ReminderStatusPreview(
    @PreviewParameter(ReminderUiStatePreviewProvider::class) state: ReminderUiState,
) {
    ManiculePreviewTheme {
        ReminderStatus(state, onRetry = {})
    }
}
