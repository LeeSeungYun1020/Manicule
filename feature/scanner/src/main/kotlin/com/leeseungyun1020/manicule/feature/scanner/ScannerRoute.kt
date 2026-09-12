package com.leeseungyun1020.manicule.feature.scanner

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ScannerRoute(
    onNavigateBack: () -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: ScannerViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val owner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        viewModel.onPermissionResult(
            granted = granted,
            shouldShowRationale = activity?.let {
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA)
            } ?: true,
        )
    }
    DisposableEffect(owner, context, viewModel) {
        fun checkPermission() {
            viewModel.onPermissionChanged(
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
            )
        }
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) checkPermission()
        }
        owner.lifecycle.addObserver(observer)
        checkPermission()
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
    ScannerScreen(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onNavigateToSearch = onNavigateToSearch,
        onUseCamera = {
            try {
                if ((uiState as? ScannerUiState.PermissionDenied)?.requiresSettings == true) {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null)),
                    )
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            } catch (_: ActivityNotFoundException) {
                viewModel.onPermissionLaunchFailed()
            } catch (_: SecurityException) {
                viewModel.onPermissionLaunchFailed()
            }
        },
        cameraPreview = {
            viewModel.reader?.let { reader ->
                CameraPreview(
                    reader = reader,
                    onInitializing = viewModel::onPreviewInitializing,
                    onReady = viewModel::onPreviewReady,
                    onFailed = viewModel::onPreviewFailed,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    )
}
