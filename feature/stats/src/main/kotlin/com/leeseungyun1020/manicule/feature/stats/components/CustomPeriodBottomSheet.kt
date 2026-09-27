package com.leeseungyun1020.manicule.feature.stats.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeBottomSheet
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.feature.stats.CustomPeriodRange
import com.leeseungyun1020.manicule.feature.stats.R
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@Composable
fun CustomPeriodBottomSheet(
    today: LocalDate,
    initialRange: CustomPeriodRange,
    onApply: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        CustomPeriodContent(
            today = today,
            initialRange = initialRange,
            onApply = onApply,
            onDismiss = onDismiss,
        )
    }
}

@Composable
internal fun CustomPeriodContent(
    today: LocalDate,
    initialRange: CustomPeriodRange,
    onApply: (LocalDate, LocalDate) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var startDateString by rememberSaveable { mutableStateOf(initialRange.start.toString()) }
    var endDateString by rememberSaveable { mutableStateOf(initialRange.end.toString()) }
    var showStartDatePicker by rememberSaveable { mutableStateOf(false) }
    var showEndDatePicker by rememberSaveable { mutableStateOf(false) }

    val startDate = remember(startDateString) {
        runCatching { LocalDate.parse(startDateString) }.getOrDefault(initialRange.start)
    }
    val endDate = remember(endDateString) {
        runCatching { LocalDate.parse(endDateString) }.getOrDefault(initialRange.end)
    }
    val range = remember(startDate, endDate) { CustomPeriodRange(startDate, endDate) }
    val error = remember(range, today) { range.validate(today) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.lg)
            .padding(bottom = MaterialTheme.spacing.xl)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = stringResource(R.string.stats_custom_period_title),
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = ManiculeIcons.Close,
                    contentDescription = stringResource(R.string.stats_close),
                )
            }
        }

        ManiculeCard(modifier = Modifier.fillMaxWidth()) {
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.stats_custom_period_start_date),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Text(
                        text = stringResource(
                            R.string.stats_single_date,
                            startDate.year,
                            startDate.monthNumber,
                            startDate.dayOfMonth,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { showStartDatePicker = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            HorizontalDivider()
            ListItem(
                headlineContent = {
                    Text(
                        text = stringResource(R.string.stats_custom_period_end_date),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                },
                trailingContent = {
                    Text(
                        text = stringResource(
                            R.string.stats_single_date,
                            endDate.year,
                            endDate.monthNumber,
                            endDate.dayOfMonth,
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button) { showEndDatePicker = true },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }

        if (error != null) {
            val errorMessage = when (error) {
                CustomPeriodRange.ValidationError.START_AFTER_END ->
                    stringResource(R.string.stats_custom_period_error_order)
                CustomPeriodRange.ValidationError.EXCEEDS_MAX_DAYS ->
                    stringResource(R.string.stats_custom_period_error_max_days)
                CustomPeriodRange.ValidationError.FUTURE_DATE ->
                    stringResource(R.string.stats_custom_period_error_future)
            }
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }

        ManiculeButton(
            onClick = {
                if (error == null) {
                    onApply(startDate, endDate)
                }
            },
            text = stringResource(R.string.stats_custom_period_apply),
            enabled = error == null,
            modifier = Modifier.fillMaxWidth(),
        )
    }

    if (showStartDatePicker) {
        StatsDatePickerDialog(
            initialDate = startDate,
            maxDate = today,
            onConfirm = { date ->
                startDateString = date.toString()
            },
            onDismiss = { showStartDatePicker = false },
        )
    }

    if (showEndDatePicker) {
        StatsDatePickerDialog(
            initialDate = endDate,
            maxDate = today,
            onConfirm = { date ->
                endDateString = date.toString()
            },
            onDismiss = { showEndDatePicker = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsDatePickerDialog(
    initialDate: LocalDate,
    maxDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val maxDateMillis = remember(maxDate) {
        maxDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
    }
    val selectableDates = remember(maxDateMillis, maxDate.year) {
        object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean = utcTimeMillis <= maxDateMillis

            override fun isSelectableYear(year: Int): Boolean = year <= maxDate.year
        }
    }
    val validInitialDate = if (initialDate > maxDate) maxDate else initialDate
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = validInitialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
        selectableDates = selectableDates,
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.fromEpochMilliseconds(millis)
                            .toLocalDateTime(TimeZone.UTC)
                            .date
                        onConfirm(selectedDate)
                    }
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.stats_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.stats_custom_period_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@ManiculePreview
@Composable
private fun CustomPeriodContentPreview() {
    val today = LocalDate(2026, 9, 24)
    ManiculePreviewTheme {
        CustomPeriodContent(
            today = today,
            initialRange = CustomPeriodRange.defaultFor(today),
            onApply = { _, _ -> },
            onDismiss = {},
        )
    }
}

@ManiculePreview
@Composable
private fun CustomPeriodContentErrorPreview() {
    val today = LocalDate(2026, 9, 24)
    ManiculePreviewTheme {
        CustomPeriodContent(
            today = today,
            initialRange = CustomPeriodRange(LocalDate(2026, 9, 20), LocalDate(2026, 9, 10)),
            onApply = { _, _ -> },
            onDismiss = {},
        )
    }
}
