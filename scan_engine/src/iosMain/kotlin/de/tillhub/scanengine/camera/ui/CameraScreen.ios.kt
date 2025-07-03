package de.tillhub.scanengine.camera.ui

import androidx.compose.foundation.layout.fillMaxSize
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

/**
 * Displays a camera preview for scanning barcodes.
 * This composable function is specific to the iOS platform.
 *
 * It initializes a [CameraController] to manage camera operations and barcode scanning.
 * A `DisposableEffect` is used to listen for device orientation changes and update the
 * camera preview's orientation accordingly. This ensures the preview remains correctly
 * oriented as the device is rotated.
 *
 * The camera preview itself is rendered using `UIKitViewController`, which allows
 * embedding UIKit views (in this case, the camera preview layer from `CameraController`)
 * within a Compose UI.
 *
 * @param modifier The modifier to be applied to the camera preview.
 * @param barcodeScanned A lambda function that is invoked when a barcode is successfully scanned.
 *                       It receives the scanned barcode string as a parameter.
 * @param onCameraError A lambda function that is invoked if an error occurs during camera initialization
 *                      or operation. It receives an error message string as a parameter.
 */
@Composable
internal actual fun cameraPreview(
    modifier: Modifier,
    barcodeScanned: (String) -> Unit,
    onCameraError: (String) -> Unit
) {
    val cameraController = remember {
        CameraController(
            barcodeScanned = barcodeScanned,
            onCameraError = onCameraError
        )
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
        modifier = Modifier.fillMaxSize(),
        factory = { cameraController },
    )
}