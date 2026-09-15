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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSnackbarHost
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.feature.settings.components.ReminderSection
import com.leeseungyun1020.manicule.feature.settings.components.ReminderUiStatePreviewProvider
import kotlinx.datetime.LocalTime

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
        Box(
            modifier = Modifier.fillMaxSize().padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = MaterialTheme.size.contentMaxWidth)
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(MaterialTheme.spacing.screenContent)
                    .testTag(SETTINGS_CONTENT_TEST_TAG),
            ) {
                ReminderSection(
                    state = uiState.reminder,
                    onEnabledChange = onReminderEnabledChange,
                    onTimeChange = onReminderTimeChange,
                    onRetry = onRetryPreferences,
                )
            }
        }
    }
}

@ManiculePreview
@Preview(name = "Phone", device = Devices.PHONE)
@Preview(name = "Foldable", device = Devices.FOLDABLE)
@Preview(name = "Tablet", device = Devices.TABLET)
@Composable
private fun SettingsScreenPreview(
    @PreviewParameter(ReminderUiStatePreviewProvider::class) reminder: ReminderUiState,
) {
    ManiculePreviewTheme {
        SettingsScreen(
            uiState = SettingsUiState(reminder),
            snackbarHostState = SnackbarHostState(),
            onReminderEnabledChange = {},
            onReminderTimeChange = {},
            onRetryPreferences = {},
        )
    }
}
