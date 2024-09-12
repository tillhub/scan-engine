package de.tillhub.scanengine.sunmi.camera

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.CameraScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class SunmiCameraScanner(
    resultCaller: ActivityResultCaller,
    private val mutableScannerEvents: MutableStateFlow<ScannerEvent>,
) : CameraScanner {

    private val scannerLauncher: ActivityResultLauncher<Unit> =
        resultCaller.registerForActivityResult(SunmiScannerActivityContract()) { result ->
            result.map { event ->
                when (event) {
                    ScannerEvent.Camera.Canceled -> mutableScannerEvents.tryEmit(event)
                    is ScannerEvent.Success -> {
                        val scanKey = (mutableScannerEvents.value as? ScannerEvent.Camera.InProgress)?.scanKey
                        mutableScannerEvents.tryEmit(event.copy(scanKey = scanKey))
                    }

                    is ScannerEvent.Camera.InProgress,
                    ScannerEvent.External.NotConnected,
                    ScannerEvent.External.Connected -> Unit
                }
            }
        }

    override fun startCameraScanner(scanKey: String?) {
        mutableScannerEvents.tryEmit(ScannerEvent.Camera.InProgress(scanKey))
        scannerLauncher.launch(Unit)
    }

    override fun observeScannerResults(): StateFlow<ScannerEvent> = mutableScannerEvents

    sealed class ScanCodeType {
        data class Type(val code: String) : ScanCodeType()
        data object Unknown : ScanCodeType()
    }

    companion object {
        const val RESPONSE_TYPE = "TYPE"
        const val RESPONSE_VALUE = "VALUE"
        const val DATA = "data"
    }
}
