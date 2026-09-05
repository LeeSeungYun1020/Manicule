package com.leeseungyun1020.manicule.feature.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ReminderConfig
import com.leeseungyun1020.manicule.feature.settings.R
import kotlinx.datetime.LocalTime

@Composable
fun ReminderToggle(
    reminder: ReminderConfig,
    formattedTime: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        ManiculeSectionHeader(title = stringResource(R.string.settings_notifications_section))
        ManiculeCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_reading_reminder)) },
                trailingContent = {
                    Switch(
                        checked = reminder.enabled,
                        onCheckedChange = null,
                        enabled = enabled,
                        modifier = Modifier.clearAndSetSemantics {},
                    )
                },
                modifier =
                    Modifier.toggleable(
                        value = reminder.enabled,
                        enabled = enabled,
                        role = Role.Switch,
                        onValueChange = onEnabledChange,
                    ),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            AnimatedVisibility(visible = reminder.enabled) {
                Column {
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.settings_reminder_time)) },
                        trailingContent = {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = formattedTime,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelLarge,
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.outline,
                                )
                            }
                        },
                        modifier = Modifier.clickable(enabled = enabled, onClick = onTimeClick),
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    )
                }
            }
        }
    }
}

@ManiculePreview
@Composable
private fun EnabledReminderTogglePreview() {
    ManiculePreviewTheme {
        ReminderToggle(
            reminder = ReminderConfig(enabled = true, time = LocalTime(21, 0)),
            formattedTime = "오후 9:00",
            enabled = true,
            onEnabledChange = {},
            onTimeClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@ManiculePreview
@Composable
private fun DisabledReminderTogglePreview() {
    ManiculePreviewTheme {
        ReminderToggle(
            reminder = ReminderConfig.Default,
            formattedTime = "오후 9:00",
            enabled = false,
            onEnabledChange = {},
            onTimeClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
