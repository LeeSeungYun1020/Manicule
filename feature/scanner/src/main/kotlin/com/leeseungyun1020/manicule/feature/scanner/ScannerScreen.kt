package com.leeseungyun1020.manicule.feature.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeEmptyState
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeIconButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeLoading
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeOutlinedButton
import com.leeseungyun1020.manicule.core.designsystem.component.ManiculeTopAppBar
import com.leeseungyun1020.manicule.core.designsystem.icon.ManiculeIcons
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreview
import com.leeseungyun1020.manicule.core.designsystem.theme.ManiculePreviewTheme
import com.leeseungyun1020.manicule.core.designsystem.theme.border
import com.leeseungyun1020.manicule.core.designsystem.theme.size
import com.leeseungyun1020.manicule.core.designsystem.theme.spacing

@Composable
internal fun ScannerScreen(
    uiState: ScannerUiState,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onUseCamera: () -> Unit,
    modifier: Modifier = Modifier,
    cameraPreview: @Composable () -> Unit = {},
) {
    when (uiState) {
        is ScannerUiState.PermissionDenied, ScannerUiState.Failed -> ScannerMessageScreen(
            uiState = uiState,
            onNavigateBack = onNavigateBack,
            onNavigateToSearch = onNavigateToSearch,
            onUseCamera = onUseCamera,
            modifier = modifier,
        )
        ScannerUiState.Initializing, ScannerUiState.Scanning, ScannerUiState.LookingUp -> Box(
            modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.scrim),
        ) {
            cameraPreview()
            BarcodeScannerOverlay(
                initializing = uiState == ScannerUiState.Initializing,
                lookingUp = uiState == ScannerUiState.LookingUp,
                onNavigateBack = onNavigateBack,
            )
        }
        is ScannerUiState.Success, ScannerUiState.NavigationDelivered -> Box(modifier = modifier.fillMaxSize())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScannerMessageScreen(
    uiState: ScannerUiState,
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onUseCamera: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val denied = uiState as? ScannerUiState.PermissionDenied
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ManiculeTopAppBar(
                title = stringResource(R.string.scanner_title),
                onNavigateBack = onNavigateBack,
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)
                .verticalScroll(rememberScrollState()).padding(MaterialTheme.spacing.screenHorizontal),
            verticalArrangement = Arrangement.Center,
        ) {
            ManiculeEmptyState(
                title = stringResource(if (denied != null) R.string.scanner_permission_title else R.string.scanner_failed_title),
                description = stringResource(
                    when {
                        denied == null -> R.string.scanner_failed_description
                        denied.launchFailed -> R.string.scanner_permission_launch_failed
                        denied.requiresSettings -> R.string.scanner_permission_settings
                        else -> R.string.scanner_permission_description
                    },
                ),
                icon = {
                    Icon(
                        imageVector = ManiculeIcons.CameraOff,
                        contentDescription = null,
                        modifier = Modifier.size(MaterialTheme.size.iconEmptyState),
                    )
                },
                actions = {
                    if (denied != null) {
                        ManiculeButton(onClick = onUseCamera, text = stringResource(R.string.scanner_use_camera))
                        ManiculeOutlinedButton(onClick = onNavigateToSearch, text = stringResource(R.string.scanner_search))
                    } else {
                        ManiculeButton(onClick = onNavigateToSearch, text = stringResource(R.string.scanner_search))
                    }
                },
            )
        }
    }
}

@Composable
private fun BarcodeScannerOverlay(
    initializing: Boolean,
    lookingUp: Boolean,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = CAMERA_SCRIM_ALPHA))
            .safeDrawingPadding()
            .padding(MaterialTheme.spacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Surface(
            modifier = Modifier.align(Alignment.Start),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = OVERLAY_ALPHA),
        ) {
            ManiculeIconButton(onClick = onNavigateBack) {
                Icon(
                    ManiculeIcons.Back,
                    contentDescription = stringResource(com.leeseungyun1020.manicule.core.designsystem.R.string.core_designsystem_back),
                )
            }
        }
        BoxWithConstraints(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            // Prototype 210x130 UI guide only: never a crop region or barcode format constraint.
            val width = minOf(
                maxWidth * VIEWFINDER_WIDTH_FRACTION,
                MaterialTheme.size.scannerViewfinderMaxWidth,
                maxHeight * VIEWFINDER_RATIO,
            )
            Box(
                modifier = Modifier.size(width, width / VIEWFINDER_RATIO)
                    .border(MaterialTheme.border.ring, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                    .testTag("scanner_viewfinder"),
                contentAlignment = Alignment.Center,
            ) {
                if (initializing || lookingUp) ManiculeLoading()
            }
        }
        Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surface.copy(alpha = OVERLAY_ALPHA)) {
            Text(
                text = stringResource(
                    when {
                        initializing -> R.string.scanner_initializing
                        lookingUp -> R.string.scanner_looking_up
                        else -> R.string.scanner_guide
                    },
                ),
                modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg, vertical = MaterialTheme.spacing.sm),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private const val VIEWFINDER_RATIO = 21f / 13f
private const val VIEWFINDER_WIDTH_FRACTION = 0.58f
private const val OVERLAY_ALPHA = 0.95f
private const val CAMERA_SCRIM_ALPHA = 0.24f

private class ScannerPreviewStates : PreviewParameterProvider<ScannerUiState> {
    override val values = sequenceOf(
        ScannerUiState.PermissionDenied(),
        ScannerUiState.PermissionDenied(requiresSettings = true),
        ScannerUiState.PermissionDenied(requiresSettings = true, launchFailed = true),
        ScannerUiState.Initializing,
        ScannerUiState.Scanning,
        ScannerUiState.LookingUp,
        ScannerUiState.Success("9780000000000"),
        ScannerUiState.NavigationDelivered,
        ScannerUiState.Failed,
    )
}

@ManiculePreview
@Preview(name = "Phone", widthDp = 360, heightDp = 640)
@Preview(name = "Landscape", widthDp = 800, heightDp = 360)
@Preview(name = "Foldable", widthDp = 673, heightDp = 841)
@Preview(name = "Tablet", widthDp = 1280, heightDp = 800)
@Composable
private fun ScannerScreenPreview(
    @PreviewParameter(ScannerPreviewStates::class) state: ScannerUiState,
) {
    ManiculePreviewTheme {
        ScannerScreen(state, onNavigateBack = {}, onNavigateToSearch = {}, onUseCamera = {})
    }
}
