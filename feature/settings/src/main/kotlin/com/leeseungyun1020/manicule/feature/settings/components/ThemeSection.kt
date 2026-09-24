package com.leeseungyun1020.manicule.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSectionHeader
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeSegmentedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextButton
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing
import com.leeseungyun1020.manicule.core.model.ThemeMode
import com.leeseungyun1020.manicule.feature.settings.R
import com.leeseungyun1020.manicule.feature.settings.ThemeUiState
import com.leeseungyun1020.manicule.feature.settings.displayedMode

@Composable
internal fun ThemeSection(
    state: ThemeUiState,
    onThemeSelected: (ThemeMode) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mode = state.displayedMode
    val labels = mapOf(
        ThemeMode.SYSTEM to stringResource(R.string.settings_theme_system),
        ThemeMode.LIGHT to stringResource(R.string.settings_theme_light),
        ThemeMode.DARK to stringResource(R.string.settings_theme_dark),
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        ManiculeSectionHeader(title = stringResource(R.string.settings_appearance_section))
        Text(
            text = stringResource(R.string.settings_theme),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (state is ThemeUiState.Content) {
            ManiculeSegmentedButton(
                options = ThemeMode.entries,
                selectedOption = mode!!,
                onOptionSelected = onThemeSelected,
                itemLabel = { labels.getValue(it) },
            )
        } else {
            Row(modifier = Modifier.fillMaxWidth().padding(MaterialTheme.spacing.sm)) {
                Text(
                    text = when (state) {
                        is ThemeUiState.Loading -> stringResource(R.string.settings_theme_loading)
                        is ThemeUiState.Error -> stringResource(R.string.settings_theme_load_error)
                        is ThemeUiState.Content -> error("unreachable")
                    },
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (state is ThemeUiState.Error) {
                    ManiculeTextButton(onClick = onRetry, text = stringResource(R.string.settings_retry))
                }
            }
            if (mode != null) {
                Text(
                    text = stringResource(R.string.settings_theme_previous, labels.getValue(mode)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@ManiculePreview
@Composable
private fun ThemeSectionPreview(
    @PreviewParameter(ThemeUiStatePreviewProvider::class) state: ThemeUiState,
) {
    ManiculePreviewTheme {
        ThemeSection(state, onThemeSelected = {}, onRetry = {})
    }
}

internal class ThemeUiStatePreviewProvider : PreviewParameterProvider<ThemeUiState> {
    override val values: Sequence<ThemeUiState> = sequenceOf(
        ThemeUiState.Loading(),
        ThemeUiState.Loading(ThemeMode.DARK),
        ThemeUiState.Error(),
        ThemeUiState.Error(ThemeMode.LIGHT),
        ThemeUiState.Content(ThemeMode.SYSTEM),
        ThemeUiState.Content(ThemeMode.LIGHT),
        ThemeUiState.Content(ThemeMode.DARK),
    )
}
