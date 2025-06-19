package de.tillhub.scanengine

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.camera.contract.CameraScanContract
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.StateFlow

interface CameraScanner {
    fun observeScannerResults(): StateFlow<ScannerEvent>

    @Composable
    fun cameraScannerLauncher(): CameraScanContract
}
