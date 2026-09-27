package com.leeseungyun1020.manicule.feature.bookdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.leeseungyun1020.manicule.core.model.ReadingRecord
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

private data class RecordFormInitialValues(
    val dateMode: DateSelectionMode,
    val customDateText: String,
    val timeMode: TimeSelectionMode,
    val customTimeText: String,
    val startPageText: String,
    val endPageText: String,
)

private fun resolveInitialFormValues(
    recordToEdit: ReadingRecord?,
    initialStartPage: Int,
    today: LocalDate,
    currentTime: LocalTime,
): RecordFormInitialValues {
    val dateMode = when {
        recordToEdit == null -> DateSelectionMode.Today
        recordToEdit.date == today -> DateSelectionMode.Today
        recordToEdit.date == today.minus(1, DateTimeUnit.DAY) -> DateSelectionMode.Yesterday
        else -> DateSelectionMode.Custom
    }
    val customDateText = recordToEdit?.date?.toString() ?: today.toString()
    val timeMode = if (recordToEdit != null) TimeSelectionMode.Custom else TimeSelectionMode.Now
    val customTimeText = if (recordToEdit != null) {
        LocalTime(recordToEdit.time.hour, recordToEdit.time.minute).toString()
    } else {
        LocalTime(currentTime.hour, currentTime.minute).toString()
    }
    val startPageText = (recordToEdit?.startPage ?: initialStartPage).toString()
    val endPageText = recordToEdit?.endPage?.toString() ?: ""
    return RecordFormInitialValues(
        dateMode = dateMode,
        customDateText = customDateText,
        timeMode = timeMode,
        customTimeText = customTimeText,
        startPageText = startPageText,
        endPageText = endPageText,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddRecordBottomSheet(
    initialStartPage: Int,
    isSaving: Boolean,
    onDismissRequest: () -> Unit,
    onSave: (LocalDate, LocalTime, Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    recordToEdit: ReadingRecord? = null,
) {
    val isEditMode = recordToEdit != null
    val targetRecordId = recordToEdit?.id ?: 0L

    val timeZone = remember { TimeZone.currentSystemDefault() }
    val today = remember { Clock.System.now().toLocalDateTime(timeZone).date }
    val currentTime = remember { Clock.System.now().toLocalDateTime(timeZone).time }

    val initialValues = remember(recordToEdit, initialStartPage, today, currentTime) {
        resolveInitialFormValues(recordToEdit, initialStartPage, today, currentTime)
    }

    var dateMode by rememberSaveable(targetRecordId) { mutableStateOf(initialValues.dateMode) }
    var customDateText by rememberSaveable(targetRecordId) { mutableStateOf(initialValues.customDateText) }
    var showDatePicker by rememberSaveable(targetRecordId) { mutableStateOf(false) }

    var timeMode by rememberSaveable(targetRecordId) { mutableStateOf(initialValues.timeMode) }
    var customTimeText by rememberSaveable(targetRecordId) { mutableStateOf(initialValues.customTimeText) }
    var showTimePicker by rememberSaveable(targetRecordId) { mutableStateOf(false) }

    var startPageText by rememberSaveable(targetRecordId) { mutableStateOf(initialValues.startPageText) }
    var endPageText by rememberSaveable(targetRecordId) { mutableStateOf(initialValues.endPageText) }

    val startPage = startPageText.toIntOrNull()
    val endPage = endPageText.toIntOrNull()
    val isPageValid = isPageRangeValid(startPage, endPage)

    val selectedDate = resolveSelectedDate(dateMode, today, LocalDate.parse(customDateText))
    val selectedTime = resolveSelectedTime(timeMode, currentTime, LocalTime.parse(customTimeText))

    val sheetState =
        rememberModalBottomSheetState(
            confirmValueChange = { targetValue -> !(isSaving && targetValue == SheetValue.Hidden) },
        )

    ManiculeBottomSheet(
        onDismissRequest = { if (!isSaving) onDismissRequest() },
        modifier = modifier,
        sheetState = sheetState,
    ) {
        RecordSheetFormContent(
            isEditMode = isEditMode,
            isSaving = isSaving,
            dateMode = dateMode,
            onDateModeSelected = { mode ->
                dateMode = mode
                if (mode == DateSelectionMode.Custom) showDatePicker = true
            },
            startPageText = startPageText,
            onStartPageChange = { startPageText = it.filter { ch -> ch.isDigit() } },
            endPageText = endPageText,
            onEndPageChange = { endPageText = it.filter { ch -> ch.isDigit() } },
            timeMode = timeMode,
            onTimeModeSelected = { mode ->
                timeMode = mode
                if (mode == TimeSelectionMode.Custom) showTimePicker = true
            },
            isPageValid = isPageValid,
            onDismissRequest = onDismissRequest,
            onSave = {
                if (startPage != null && endPage != null && isPageValid) {
                    onSave(selectedDate, selectedTime, startPage, endPage)
                }
            },
        )
    }

    if (showDatePicker) {
        RecordDatePickerDialog(
            initialDate = selectedDate,
            onConfirm = { customDateText = it.toString() },
            onDismiss = { showDatePicker = false },
        )
    }

    if (showTimePicker) {
        RecordTimePickerDialog(
            initialTime = selectedTime,
            onConfirm = { customTimeText = it.toString() },
            onDismiss = { showTimePicker = false },
        )
    }
}

@Composable
private fun RecordSheetFormContent(
    isEditMode: Boolean,
    isSaving: Boolean,
    dateMode: DateSelectionMode,
    onDateModeSelected: (DateSelectionMode) -> Unit,
    startPageText: String,
    onStartPageChange: (String) -> Unit,
    endPageText: String,
    onEndPageChange: (String) -> Unit,
    timeMode: TimeSelectionMode,
    onTimeModeSelected: (TimeSelectionMode) -> Unit,
    isPageValid: Boolean,
    onDismissRequest: () -> Unit,
    onSave: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.screenHorizontal)
            .padding(bottom = MaterialTheme.spacing.xl)
            .imePadding()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (isEditMode) {
                    stringResource(R.string.book_detail_edit_record_title)
                } else {
                    stringResource(R.string.book_detail_add_record_title)
                },
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = onDismissRequest,
                enabled = !isSaving,
            ) {
                Icon(
                    imageVector = ManiculeIcons.Close,
                    contentDescription = stringResource(R.string.book_detail_cancel),
                )
            }
        }

        RecordDateSection(
            selectedMode = dateMode,
            onModeSelected = onDateModeSelected,
        )

        val startPage = startPageText.toIntOrNull()
        val endPage = endPageText.toIntOrNull()
        RecordPageSection(
            startPageText = startPageText,
            onStartPageChange = onStartPageChange,
            endPageText = endPageText,
            onEndPageChange = onEndPageChange,
            isError = endPage != null && startPage != null && endPage < startPage,
        )

        RecordTimeSection(
            selectedMode = timeMode,
            onModeSelected = onTimeModeSelected,
        )

        ManiculeButton(
            onClick = onSave,
            modifier = Modifier.fillMaxWidth(),
            text = if (isEditMode) {
                stringResource(R.string.book_detail_edit_record_button)
            } else {
                stringResource(R.string.book_detail_add_record_button)
            },
            enabled = isPageValid && !isSaving,
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

@ManiculePreview
@Composable
private fun EditRecordBottomSheetPreview() {
    ManiculePreviewTheme {
        AddRecordBottomSheet(
            initialStartPage = 1,
            isSaving = false,
            onDismissRequest = {},
            onSave = { _, _, _, _ -> },
            recordToEdit = ReadingRecord(
                id = 1L,
                isbn = "123",
                date = LocalDate(2026, 7, 8),
                time = LocalTime(21, 12),
                startPage = 43,
                endPage = 68,
            ),
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
