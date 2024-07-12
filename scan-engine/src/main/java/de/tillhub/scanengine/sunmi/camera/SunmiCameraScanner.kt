package de.tillhub.scanengine.sunmi.camera

import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.CameraScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class SunmiCameraScanner(
    resultCaller: ActivityResultCaller,
    private val mutableScanEvents: MutableStateFlow<ScanEvent>,
) : CameraScanner {

    private val scannerLauncher: ActivityResultLauncher<Unit> =
        resultCaller.registerForActivityResult(SunmiScannerActivityContract()) { result ->
            result.map { event ->
                when (event) {
                    ScanEvent.Canceled -> mutableScanEvents.tryEmit(event)
                    is ScanEvent.Success -> {
                        val scanKey = (mutableScanEvents.value as? ScanEvent.InProgress)?.scanKey
                        mutableScanEvents.tryEmit(event.copy(scanKey = scanKey))
                    }

                    is ScanEvent.InProgress,
                    ScanEvent.NotConnected,
                    ScanEvent.Connected -> Unit
                }
            }
        }

    override fun startCameraScanner(scanKey: String?) {
        mutableScanEvents.tryEmit(ScanEvent.InProgress(scanKey))
        scannerLauncher.launch(Unit)
    }

    override fun observeScannerResults(): StateFlow<ScanEvent> = mutableScanEvents

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
