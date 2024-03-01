package de.tillhub.scanengine.sunmi

import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.Scanner.Companion.CAMERA_SCANNER_KEY
import de.tillhub.scanengine.ScannerImpl
import kotlinx.coroutines.flow.MutableSharedFlow

class SunmiScanner(
    private val activityResultRegistry: ActivityResultRegistry,
    savedStateRegistry: SavedStateRegistry,
    mutableScanEvents: MutableSharedFlow<ScanEvent>,
) : ScannerImpl(savedStateRegistry, mutableScanEvents) {

    private lateinit var scannerLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        scannerLauncher = activityResultRegistry.register(
            CAMERA_SCANNER_KEY,
            owner,
            SunmiScannerActivityContract()
        ) { result ->
            result.map {
                when (it) {
                    ScanEvent.Canceled -> mutableScanEvents.tryEmit(it)
                    is ScanEvent.Success -> mutableScanEvents.tryEmit(it.copy(scanKey = scanKey))
                    ScanEvent.InProgress -> Unit
                }
            }
        }
    }

    override fun startCameraScanner(scanKey: String?) {
        mutableScanEvents.tryEmit(ScanEvent.InProgress)
        this.scanKey = scanKey
        scannerLauncher.launch(scanIntent())
    }

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
