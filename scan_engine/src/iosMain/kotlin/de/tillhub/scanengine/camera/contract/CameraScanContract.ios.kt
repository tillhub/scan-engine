package de.tillhub.scanengine.camera.contract

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import de.tillhub.scanengine.camera.ui.CameraScreen
import de.tillhub.scanengine.data.ScannerEvent
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController

@Composable
actual fun rememberCameraScanLauncher(
    onResult: (ScannerEvent) -> Unit
): CameraScanContract = remember {
    object : CameraScanContract {
        private var viewController: UIViewController? = null

        override fun launchCameraScanner(scanKey: String?) {
            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return

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

        private fun dismiss() {
            viewController?.dismissViewControllerAnimated(true) {
                viewController = null
            }
        }
    }
}