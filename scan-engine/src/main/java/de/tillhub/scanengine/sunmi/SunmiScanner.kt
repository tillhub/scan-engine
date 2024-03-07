package de.tillhub.scanengine.sunmi

import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.Scanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow

class SunmiScanner(
    resultCaller: ActivityResultCaller,
    private val mutableScanEvents: MutableStateFlow<ScanEvent>,
) : Scanner {

    private val scannerLauncher: ActivityResultLauncher<Intent> =
        resultCaller.registerForActivityResult(SunmiScannerActivityContract()) { result ->
            result.map { event ->
                when (event) {
                    ScanEvent.Canceled -> mutableScanEvents.tryEmit(event)
                    is ScanEvent.Success -> {
                        val scanKey = (mutableScanEvents.value as? ScanEvent.InProgress)?.scanKey
                        mutableScanEvents.tryEmit(event.copy(scanKey = scanKey))
                    }

                    is ScanEvent.InProgress,
                    ScanEvent.Idle -> Unit
                }
            }
        }

    override fun startCameraScanner(scanKey: String?) {
        mutableScanEvents.tryEmit(ScanEvent.InProgress(scanKey))
        scannerLauncher.launch(scanIntent())
    }

    override fun observeScannerResults(): SharedFlow<ScanEvent> = mutableScanEvents

    sealed class ScanCodeType {
        data class Type(val code: String) : ScanCodeType()
        data object Unknown : ScanCodeType()
    }

    companion object {
        const val RESPONSE_TYPE = "TYPE"
        const val RESPONSE_VALUE = "VALUE"
        const val DATA = "data"

        private fun scanIntent(scanMultipleCodes: Boolean = false): Intent =
            Intent("com.summi.scan").apply {
                setPackage("com.sunmi.sunmiqrcodescanner")

                // Additional intent options:
                //
                // The current preview resolution ,PPI_1920_1080 = 0X0001;PPI_1280_720 = 0X0002;PPI_BEST = 0X0003;
                // putExtra("CURRENT_PPI", 0X0003)
                //
                // Whether to identify inverse code
                // putExtra("IDENTIFY_INVERSE_QR_CODE", true)
                //
                // Vibrate after scanning, default false, only support M1 right now.
                // putExtra("PLAY_VIBRATE", false)

                // Prompt tone after scanning (default true)
                putExtra("PLAY_SOUND", true)

                // Whether to display the settings button at the top-right corner (default true)
                putExtra("IS_SHOW_SETTING", true)

                // Whether to display the album button (default true)
                putExtra("IS_SHOW_ALBUM", false)

                // Whether to identify several codes at once (default false)
                putExtra("IDENTIFY_MORE_CODE", scanMultipleCodes)
            }
    }
}
