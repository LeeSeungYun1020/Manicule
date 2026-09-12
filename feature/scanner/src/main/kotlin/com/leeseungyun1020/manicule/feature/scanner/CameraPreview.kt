package com.leeseungyun1020.manicule.feature.scanner

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Handler
import android.os.Looper
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.doOnAttach
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.leeseungyun1020.manicule.core.scanner.BarcodeReader
import kotlinx.coroutines.CancellationException

@Composable
internal fun CameraPreview(
    reader: BarcodeReader,
    onInitializing: () -> Unit,
    onReady: () -> Unit,
    onFailed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember(context) {
        PreviewView(context).apply { scaleType = PreviewView.ScaleType.FILL_CENTER }
    }
    val initializing by rememberUpdatedState(onInitializing)
    val ready by rememberUpdatedState(onReady)
    val failed by rememberUpdatedState(onFailed)
    AndroidView(factory = { previewView }, modifier = modifier)
    DisposableEffect(reader, lifecycleOwner, previewView) {
        initializing()
        val session = CameraPreviewSession(onReady = { ready() }, onFailed = { failed() })
        val destroyObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) session.close()
        }
        lifecycleOwner.lifecycle.addObserver(destroyObserver)
        try {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener(
                {
                    session.attach {
                        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) throw CancellationException()
                        CameraXPreviewBinding(future.get(), lifecycleOwner, previewView, reader.imageAnalysis, session::fail)
                    }
                },
                ContextCompat.getMainExecutor(context),
            )
        } catch (exception: IllegalStateException) {
            session.fail(exception)
        }
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(destroyObserver)
            session.close()
        }
    }
}

internal class CameraXPreviewBinding(
    private val provider: ProcessCameraProvider,
    private val lifecycleOwner: LifecycleOwner,
    private val previewView: PreviewView,
    private val imageAnalysis: ImageAnalysis,
    private val onError: (Exception) -> Unit,
) : PreviewBinding {
    internal val preview = Preview.Builder().build()
    private val displayManager = previewView.context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    private var closed = false
    private var listening = false
    private var cameraState: LiveData<CameraState>? = null
    private val cameraStateObserver = Observer<CameraState> { state ->
        if (!closed && state.error != null) onError(IllegalStateException("Camera unavailable: ${state.error?.code}"))
    }
    private val rotationTracker = DisplayRotationTracker(
        displayId = { previewView.display?.displayId },
        rotation = { previewView.display?.rotation },
        applyRotation = ::applyDisplayRotation,
    )
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayChanged(displayId: Int) {
            if (!closed) rotationTracker.onDisplayChanged(displayId)
        }

        override fun onDisplayAdded(displayId: Int) = Unit

        override fun onDisplayRemoved(displayId: Int) = Unit
    }
    private val lifecycleObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_START -> startListening()
            Lifecycle.Event.ON_RESUME -> rotationTracker.refresh()
            Lifecycle.Event.ON_STOP -> stopListening()
            Lifecycle.Event.ON_DESTROY -> close()
            else -> Unit
        }
    }

    override fun bind() {
        if (lifecycleOwner.lifecycle.currentState == Lifecycle.State.DESTROYED) throw CancellationException()
        check(provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) { "Rear camera unavailable" }
        rotationTracker.refresh()
        preview.setSurfaceProvider(previewView.surfaceProvider)
        val camera = provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageAnalysis)
        cameraState = camera.cameraInfo.cameraState
        cameraState?.observe(lifecycleOwner, cameraStateObserver)
        if (closed) return
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)
        // AndroidView may not be attached when an already-resolved provider completes.
        previewView.doOnAttach { if (!closed) rotationTracker.refresh() }
    }

    internal fun applyDisplayRotation(rotation: Int) {
        preview.targetRotation = rotation
        imageAnalysis.targetRotation = rotation
    }

    private fun startListening() {
        if (listening || closed) return
        displayManager.registerDisplayListener(displayListener, Handler(Looper.getMainLooper()))
        listening = true
        rotationTracker.refresh()
    }

    private fun stopListening() {
        if (!listening) return
        displayManager.unregisterDisplayListener(displayListener)
        listening = false
    }

    override fun close() {
        if (closed) return
        closed = true
        stopListening()
        lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
        cameraState?.removeObserver(cameraStateObserver)
        cameraState = null
        provider.unbind(preview, imageAnalysis)
        preview.setSurfaceProvider(null)
    }
}
