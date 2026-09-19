package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeBottomSheet
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSegmentedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextField
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.feature.bookdetail.R
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

private enum class DateSelectionMode {
    Today,
    Yesterday,
    Custom,
}

private enum class TimeSelectionMode {
    Now,
    Custom,
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddRecordBottomSheet(
    initialStartPage: Int,
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (LocalDate, LocalTime, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(timeZone).date }
    val currentTime = remember { Clock.System.now().toLocalDateTime(timeZone).time }

    var dateMode by remember { mutableStateOf(DateSelectionMode.Today) }
    var customDate by remember { mutableStateOf(today) }
    var showDatePicker by remember { mutableStateOf(false) }

    var timeMode by remember { mutableStateOf(TimeSelectionMode.Now) }
    var customTime by remember { mutableStateOf(LocalTime(currentTime.hour, currentTime.minute)) }
    var showTimePicker by remember { mutableStateOf(false) }

    var startPageText by remember { mutableStateOf(initialStartPage.toString()) }
    var endPageText by remember { mutableStateOf("") }

    val startPage = startPageText.toIntOrNull()
    val endPage = endPageText.toIntOrNull()
    val isPageValid = isPageRangeValid(startPage, endPage)

    val selectedDate = resolveSelectedDate(dateMode, today, customDate)
    val selectedTime = resolveSelectedTime(timeMode, currentTime, customTime)

    ManiculeBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
                .padding(bottom = MaterialTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.book_detail_add_record_title),
                    style = MaterialTheme.typography.titleMedium,
                )
                IconButton(onClick = onDismissRequest) {
                    Icon(
                        imageVector = ManiculeIcons.Close,
                        contentDescription = stringResource(R.string.book_detail_cancel),
                    )
                }
            }

            RecordDateSection(
                selectedMode = dateMode,
                onModeSelected = { mode ->
                    dateMode = mode
                    if (mode == DateSelectionMode.Custom) showDatePicker = true
                },
            )

            RecordPageSection(
                startPageText = startPageText,
                onStartPageChange = { startPageText = it.filter { ch -> ch.isDigit() } },
                endPageText = endPageText,
                onEndPageChange = { endPageText = it.filter { ch -> ch.isDigit() } },
                isError = endPage != null && startPage != null && endPage < startPage,
            )

            RecordTimeSection(
                selectedMode = timeMode,
                onModeSelected = { mode ->
                    timeMode = mode
                    if (mode == TimeSelectionMode.Custom) showTimePicker = true
                },
            )

            ManiculeButton(
                onClick = {
                    if (startPage != null && endPage != null && isPageValid) {
                        onSave(selectedDate, selectedTime, startPage, endPage)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.book_detail_add_record_button),
                enabled = isPageValid && !isSaving,
            )
        }
    }

    if (showDatePicker) {
        RecordDatePickerDialog(
            initialDate = selectedDate,
            onConfirm = { customDate = it },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        RecordTimePickerDialog(
            initialTime = selectedTime,
            onConfirm = { customTime = it },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun RecordDateSection(
    selectedMode: DateSelectionMode,
    onModeSelected: (DateSelectionMode) -> Unit,
) {
    val todayLabel = stringResource(R.string.book_detail_record_date_today)
    val yesterdayLabel = stringResource(R.string.book_detail_record_date_yesterday)
    val customLabel = stringResource(R.string.book_detail_record_date_custom)

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.book_detail_record_date),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ManiculeSegmentedButton(
            options = DateSelectionMode.entries,
            selectedOption = selectedMode,
            onOptionSelected = onModeSelected,
            itemLabel = { mode ->
                when (mode) {
                    DateSelectionMode.Today -> todayLabel
                    DateSelectionMode.Yesterday -> yesterdayLabel
                    DateSelectionMode.Custom -> customLabel
                }
            },
        )
    }
}

@Composable
private fun RecordPageSection(
    startPageText: String,
    onStartPageChange: (String) -> Unit,
    endPageText: String,
    onEndPageChange: (String) -> Unit,
    isError: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        ManiculeTextField(
            value = startPageText,
            onValueChange = onStartPageChange,
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.book_detail_record_start_page),
            keyboardType = KeyboardType.Number,
        )
        ManiculeTextField(
            value = endPageText,
            onValueChange = onEndPageChange,
            modifier = Modifier.weight(1f),
            label = stringResource(R.string.book_detail_record_end_page),
            keyboardType = KeyboardType.Number,
            isError = isError,
            supportingText = if (isError) {
                { Text(stringResource(R.string.book_detail_record_page_validation_error)) }
            } else {
                null
            },
        )
    }
}

@Composable
private fun RecordTimeSection(
    selectedMode: TimeSelectionMode,
    onModeSelected: (TimeSelectionMode) -> Unit,
) {
    val nowLabel = stringResource(R.string.book_detail_record_time_now)
    val customLabel = stringResource(R.string.book_detail_record_time_custom)

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
        Text(
            text = stringResource(R.string.book_detail_record_time),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ManiculeSegmentedButton(
            options = TimeSelectionMode.entries,
            selectedOption = selectedMode,
            onOptionSelected = onModeSelected,
            itemLabel = { mode ->
                when (mode) {
                    TimeSelectionMode.Now -> nowLabel
                    TimeSelectionMode.Custom -> customLabel
                }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordDatePickerDialog(
    initialDate: LocalDate,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        onConfirm(Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date)
                    }
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.book_detail_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.book_detail_cancel))
            }
        },
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordTimePickerDialog(
    initialTime: LocalTime,
    onConfirm: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.book_detail_record_time)) },
        text = { TimePicker(state = timePickerState) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(LocalTime(timePickerState.hour, timePickerState.minute))
                    onDismiss()
                },
            ) {
                Text(stringResource(R.string.book_detail_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.book_detail_cancel))
            }
        },
    )
}

@ManiculePreview
@Composable
private fun AddRecordBottomSheetPreview() {
    ManiculePreviewTheme {
        AddRecordBottomSheet(
            initialStartPage = 1,
            isSaving = false,
            onDismissRequest = {},
            onSave = { _, _, _, _ -> },
        )
    }
}

private fun isPageRangeValid(
    startPage: Int?,
    endPage: Int?,
): Boolean = startPage != null && startPage >= 1 && endPage != null && endPage >= startPage

private fun resolveSelectedDate(
    mode: DateSelectionMode,
    today: LocalDate,
    customDate: LocalDate,
): LocalDate =
    when (mode) {
        DateSelectionMode.Today -> today
        DateSelectionMode.Yesterday -> today.minus(1, DateTimeUnit.DAY)
        DateSelectionMode.Custom -> customDate
    }

private fun resolveSelectedTime(
    mode: TimeSelectionMode,
    currentTime: LocalTime,
    customTime: LocalTime,
): LocalTime =
    when (mode) {
        TimeSelectionMode.Now -> LocalTime(currentTime.hour, currentTime.minute)
        TimeSelectionMode.Custom -> customTime
    }
