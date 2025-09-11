package de.tillhub.scanengine

import androidx.compose.runtime.Composable
import de.tillhub.scanengine.camera.contract.CameraScanContract
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface that describes a component that provides barcode scanning functionality using the
 * device's camera.
 */
interface CameraScanner {
    /**
     * Observe the results of barcode scanning operations.
     *
     * @return A [StateFlow] that emits [ScannerEvent] objects representing the outcome of scanning
     * attempts. This flow can be used to react to successful scans, errors, or other events
     * related to the scanning process.
     */
    fun observeScannerResults(): StateFlow<ScannerEvent>

    /**
     * Provides a [CameraScanContract] that can be used to launch the camera scanner and
     * receive scan results.
     *
     * This function is composable, meaning it can be used within a Compose UI hierarchy.
     *
     * @return A [CameraScanContract] instance.
     */
    @Composable
    fun cameraScannerLauncher(): CameraScanContract
}
