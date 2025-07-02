package de.tillhub.scanengine.camera

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.camera.contract.CameraScanContract
import de.tillhub.scanengine.camera.contract.rememberCameraScanLauncher
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * A default implementation of [CameraScanner] that uses a [MutableStateFlow] to emit [ScannerEvent]s
 * and a [CameraScanContract] to launch the camera scanner.
 *
 * @param mutableScannerEvents The [MutableStateFlow] to emit [ScannerEvent]s to.
 */
internal class DefaultCameraScanner(
    private val mutableScannerEvents: MutableStateFlow<ScannerEvent>,
) : CameraScanner {

    override fun observeScannerResults(): StateFlow<ScannerEvent> = mutableScannerEvents

    @Composable
    override fun cameraScannerLauncher(): CameraScanContract =
        rememberCameraScanLauncher { result ->
            mutableScannerEvents.tryEmit(result)
        }
}