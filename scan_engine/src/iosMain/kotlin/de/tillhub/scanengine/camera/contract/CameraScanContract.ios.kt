package de.tillhub.scanengine.camera.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import de.tillhub.scanengine.camera.ui.CameraScreen
import de.tillhub.scanengine.data.ScannerEvent
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

/**
 * iOS implementation of a [CameraScanContract] using a [UIViewController] to present the camera
 * preview.
 *
 * @param onResult a callback for the result of the camera scan.
 */
@Composable
internal actual fun rememberCameraScanLauncher(
    onResult: (ScannerEvent) -> Unit
): CameraScanContract = remember {
    object : CameraScanContract {
        private var viewController: UIViewController? = null

        /**
         * Launches the camera scanner.
         *
         * This function presents a [ComposeUIViewController] containing the [CameraScreen]
         * composable. The [CameraScreen] handles the camera preview and barcode scanning.
         *
         * @param scanKey An optional key to identify the scan session. This key will be included
         * in the [ScannerEvent.ScanResult] when a barcode is successfully scanned.
         */
        override fun launchCameraScanner(scanKey: String?) {
            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return

            onResult.invoke(ScannerEvent.Camera.InProgress(scanKey))

            viewController = ComposeUIViewController {
                CameraScreen(
                    onResult = {
                        onResult(
                            ScannerEvent.ScanResult(
                                value = it,
                                scanKey = scanKey
                            )
                        )
                        dismiss()
                    },
                    onDismiss = {
                        onResult(ScannerEvent.Camera.Canceled)
                        dismiss()
                    }
                )
            }

            viewController?.let { vc ->
                rootVC.presentViewController(vc, animated = true, completion = null)
            }
        }

        /**
         * Dismisses the camera scanner view controller.
         *
         * This function dismisses the currently presented [UIViewController] that hosts the
         * camera preview. It also nullifies the `viewController` reference after dismissal.
         */
        private fun dismiss() {
            viewController?.dismissViewControllerAnimated(true) {
                viewController = null
            }
        }
    }
}