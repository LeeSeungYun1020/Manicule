package com.leeseungyun1020.manicule.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeCard
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeErrorState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTextButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

@Composable
fun LicensesRoute(
    onNavigateBack: () -> Unit,
    viewModel: LicensesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    LicensesScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onRetry = viewModel::retry,
        onOpenUrl = { url ->
            runCatching { uriHandler.openUri(url) }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    uiState: LicensesUiState,
    onNavigateBack: () -> Unit,
    onRetry: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var selectedLibrary by rememberSaveable { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ManiculeTopAppBar(
                title = stringResource(R.string.settings_licenses_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { contentPadding ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(contentPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            when (uiState) {
                is LicensesUiState.Loading -> {
                    ManiculeLoading(
                        modifier = Modifier.fillMaxSize(),
                    )
                }

                is LicensesUiState.Error -> {
                    ManiculeErrorState(
                        title = stringResource(R.string.settings_licenses_load_error),
                        icon = ManiculeIcons.NetworkError,
                        onRetry = onRetry,
                        modifier = Modifier.padding(MaterialTheme.spacing.screenContent),
                    )
                }

                is LicensesUiState.Success -> {
                    LazyColumn(
                        modifier =
                            Modifier
                                .widthIn(max = MaterialTheme.size.contentMaxWidth)
                                .fillMaxWidth()
                                .fillMaxHeight(),
                        contentPadding = MaterialTheme.spacing.screenContent,
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                    ) {
                        item {
                            Text(
                                text = stringResource(R.string.settings_licenses_description),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = MaterialTheme.spacing.xs),
                            )
                        }
                        items(
                            items = uiState.libraries,
                            key = { it.name },
                        ) { library ->
                            LicenseItemCard(
                                library = library,
                                onViewFullText = { selectedLibrary = library.name },
                                onOpenUrl = onOpenUrl,
                            )
                        }
                    }

                    if (selectedLibrary != null) {
                        LicenseFullTextDialog(
                            licenseText = uiState.licenseText,
                            onDismiss = { selectedLibrary = null },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LicenseItemCard(
    library: OpenSourceLibrary,
    onViewFullText: () -> Unit,
    onOpenUrl: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ManiculeCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = library.license,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = library.copyright,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (library.url != null) {
                    ManiculeTextButton(
                        onClick = { onOpenUrl(library.url) },
                        text = stringResource(R.string.settings_licenses_view_details),
                    )
                }
                ManiculeTextButton(
                    onClick = onViewFullText,
                    text = stringResource(R.string.settings_licenses_view_full_text),
                )
            }
        }
    }
}

@Composable
private fun LicenseFullTextDialog(
    licenseText: String,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.85f),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(MaterialTheme.spacing.xl),
            ) {
                Text(
                    text = stringResource(R.string.settings_licenses_title),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.md))
                Box(
                    modifier =
                        Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = licenseText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.lg))
                ManiculeButton(
                    onClick = onDismiss,
                    text = stringResource(R.string.settings_close),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@ManiculePreview
@Composable
private fun LicensesScreenSuccessPreview() {
    ManiculePreviewTheme {
        LicensesScreen(
            uiState =
                LicensesUiState.Success(
                    libraries =
                        listOf(
                            OpenSourceLibrary(
                                name = "AndroidX & Jetpack Compose",
                                copyright = "Copyright The Android Open Source Project",
                                license = "Apache License 2.0",
                                url = "https://developer.android.com/jetpack",
                            ),
                            OpenSourceLibrary(
                                name = "Coil",
                                copyright = "Copyright Coil Contributors",
                                license = "Apache License 2.0",
                                url = "https://coil-kt.github.io/coil/",
                            ),
                        ),
                    licenseText = "Apache License Version 2.0 full text...",
                ),
            onNavigateBack = {},
            onRetry = {},
            onOpenUrl = {},
        )
    }
}

@ManiculePreview
@Composable
private fun LicensesScreenLoadingPreview() {
    ManiculePreviewTheme {
        LicensesScreen(
            uiState = LicensesUiState.Loading,
            onNavigateBack = {},
            onRetry = {},
            onOpenUrl = {},
        )
    }
}

@ManiculePreview
@Composable
private fun LicensesScreenErrorPreview() {
    ManiculePreviewTheme {
        LicensesScreen(
            uiState = LicensesUiState.Error,
            onNavigateBack = {},
            onRetry = {},
            onOpenUrl = {},
        )
    }
}
