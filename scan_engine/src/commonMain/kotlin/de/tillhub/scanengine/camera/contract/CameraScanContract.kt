package de.tillhub.scanengine.camera.contract

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.data.ScannerEvent

interface CameraScanContract {
    fun launchCameraScanner(scanKey: String? = null)
}

@Composable
expect fun rememberCameraScanLauncher(
    onResult: (ScannerEvent) -> Unit
): CameraScanContract