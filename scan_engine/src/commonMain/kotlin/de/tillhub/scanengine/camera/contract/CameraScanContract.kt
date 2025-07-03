package de.tillhub.scanengine.camera.contract

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.data.ScannerEvent

/**
 * Interface that can be used to scan barcodes with the device's camera.
 */
interface CameraScanContract {
    fun launchCameraScanner(scanKey: String? = null)
}

/**
 * A Composable function that remembers a [CameraScanContract] across recompositions.
 *
 * This function is designed to be used in Compose UI to get an instance of [CameraScanContract]
 * which can then be used to launch the camera scanner. The provided [onResult] lambda
 * will be invoked when the scanner produces a result (either a successful scan or an error).
 *
 * @param onResult A lambda function that will be called with a [ScannerEvent]
 *                 representing the outcome of the scan operation. This can be a
 *                 [ScannerEvent.Success] containing the scanned data, or an
 *                 intermediary state such as [ScannerEvent.InProgress].
 * @return An instance of [CameraScanContract] that can be used to initiate a camera scan.
 */
@Composable
internal expect fun rememberCameraScanLauncher(
    onResult: (ScannerEvent) -> Unit
): CameraScanContract