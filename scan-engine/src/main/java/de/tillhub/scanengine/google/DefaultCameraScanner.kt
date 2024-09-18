package de.tillhub.scanengine.google

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.CameraScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DefaultCameraScanner(
    resultCaller: ActivityResultCaller,
    private val mutableScannerEvents: MutableStateFlow<ScannerEvent>,
) : CameraScanner {

    private val scannerLauncher: ActivityResultLauncher<Unit> =
        resultCaller.registerForActivityResult(DefaultScannerActivityContract()) { result ->
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

    override fun startCameraScanner(scanKey: String?) {
        mutableScannerEvents.tryEmit(ScannerEvent.Camera.InProgress(scanKey))
        scannerLauncher.launch(Unit)
    }

    override fun observeScannerResults(): StateFlow<ScannerEvent> = mutableScannerEvents
}
