package de.tillhub.scanengine.camera.ui

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.camera.CameraController
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
actual fun cameraPreview(
    modifier: Modifier,
    scanKey: String?,
    scannerEvents: MutableStateFlow<ScannerEvent>
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val controller = remember {
        CameraController(context, lifecycleOwner, scannerEvents).also {
            it.setScanKey(scanKey)
        }
    }

    DisposableEffect(previewView) {
        controller.bindCamera(previewView) {}
        onDispose {
            controller.stopSession()
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier,
    )
}