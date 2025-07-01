package de.tillhub.scanengine.camera.contract

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.data.ScannerEvent

/**
 * Interface that can be used to scan barcodes with the device's camera.
 */
interface CameraScanContract {
    fun launchCameraScanner(scanKey: String? = null)
}

@Composable
expect fun rememberCameraScanLauncher(
    onResult: (ScannerEvent) -> Unit
): CameraScanContract