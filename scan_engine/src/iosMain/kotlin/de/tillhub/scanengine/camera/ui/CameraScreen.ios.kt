package de.tillhub.scanengine.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import de.tillhub.scanengine.camera.CameraController
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSNotificationCenter
import platform.UIKit.UIDeviceOrientationDidChangeNotification

@Composable
actual fun cameraPreview(
    modifier: Modifier,
    barcodeScanned: (String) -> Unit
) {
    val cameraController = remember {
        CameraController(barcodeScanned = barcodeScanned)
    }

    DisposableEffect(Unit) {
        val notificationCenter = NSNotificationCenter.defaultCenter
        val observer = notificationCenter.addObserverForName(
            UIDeviceOrientationDidChangeNotification,
            null,
            null
        ) { _ ->
            cameraController.getCameraPreviewLayer()?.connection?.videoOrientation =
                cameraController.currentVideoOrientation()
        }

        onDispose {
            notificationCenter.removeObserver(observer)
        }
    }

    UIKitViewController(
        factory = { cameraController },
        modifier = modifier,
    )
}