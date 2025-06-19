package de.tillhub.scanengine.camera

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.camera.contract.CameraScanContract
import de.tillhub.scanengine.camera.contract.rememberCameraScanLauncher
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DefaultCameraScanner(
    private val mutableScannerEvents: MutableStateFlow<ScannerEvent>,
) : CameraScanner {

    override fun observeScannerResults(): StateFlow<ScannerEvent> = mutableScannerEvents

    @Composable
    override fun cameraScannerLauncher(): CameraScanContract =
        rememberCameraScanLauncher { result ->
            when (result) {
                ScannerEvent.Camera.Canceled -> mutableScannerEvents.tryEmit(result)
                is ScannerEvent.ScanResult -> {
                    val scanKey = (mutableScannerEvents.value as? ScannerEvent.Camera.InProgress)?.scanKey
                    mutableScannerEvents.tryEmit(result.copy(scanKey = scanKey))
                }

                is ScannerEvent.Camera.InProgress,
                is ScannerEvent.External.Connecting,
                ScannerEvent.External.NotConnected,
                ScannerEvent.External.Connected -> Unit
            }
        }
}