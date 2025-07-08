package de.tillhub.scanengine.camera.ui

import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import de.tillhub.scanengine.camera.CameraController

/**
 * A composable function that displays a camera preview for barcode scanning.
 *
 * This function utilizes Android's CameraX library to display a live camera feed
 * within a Jetpack Compose UI. It's designed to detect barcodes and report them
 * via the [barcodeScanned] callback. Any camera-related errors are reported
 * through the [onCameraError] callback.
 *
 * The camera session is managed by a [CameraController] which is lifecycle-aware
 * and handles binding the camera to the [PreviewView]. The session is automatically
 * started when the composable enters the composition and stopped when it leaves.
 *
 * @param modifier A [Modifier] to be applied to the camera preview.
 * @param barcodeScanned A lambda function that will be invoked with the scanned barcode string
 *                       when a barcode is successfully detected.
 * @param onCameraError A lambda function that will be invoked with an error message string
 *                      if any issue occurs during camera initialization or operation.
 */
@Composable
internal actual fun cameraPreview(
    modifier: Modifier,
    barcodeScanned: (String) -> Unit,
    onCameraError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    val previewView = remember { PreviewView(context) }
    val controller = remember {
        CameraController(context, lifecycleOwner, barcodeScanned)
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
