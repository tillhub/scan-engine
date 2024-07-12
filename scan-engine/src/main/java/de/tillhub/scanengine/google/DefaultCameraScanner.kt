package de.tillhub.scanengine.google

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.CameraScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class DefaultCameraScanner(
    resultCaller: ActivityResultCaller,
    private val mutableScanEvents: MutableStateFlow<ScanEvent>,
) : CameraScanner {

    private val scannerLauncher: ActivityResultLauncher<Unit> =
        resultCaller.registerForActivityResult(DefaultScannerActivityContract()) { result ->
            when (result) {
                ScanEvent.Canceled -> mutableScanEvents.tryEmit(result)
                is ScanEvent.Success -> {
                    val scanKey = (mutableScanEvents.value as? ScanEvent.InProgress)?.scanKey
                    mutableScanEvents.tryEmit(result.copy(scanKey = scanKey))
                }

                is ScanEvent.InProgress,
                ScanEvent.NotConnected,
                ScanEvent.Connected -> Unit
            }
        }

    override fun startCameraScanner(scanKey: String?) {
        mutableScanEvents.tryEmit(ScanEvent.InProgress(scanKey))
        scannerLauncher.launch(Unit)
    }

    override fun observeScannerResults(): StateFlow<ScanEvent> = mutableScanEvents
}
