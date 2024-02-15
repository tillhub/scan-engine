package de.tillhub.scanengine.sunmi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScanEventProvider
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.Scanner.Companion.CAMERA_SCANNER_KEY
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber

class SunmiScanner(
    private val activity: ComponentActivity,
    private val scanEventProvider: ScanEventProvider = ScanEventProvider(),
) : Scanner {

    private lateinit var sunmiScannerLauncher: ActivityResultLauncher<Intent>

    private val broadcastReceiver = SunmiBarcodeScannerBroadcastReceiver(scanEventProvider)

    private var scanKey: String? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        with(activity.savedStateRegistry) {
            registerSavedStateProvider(PROVIDER, this@SunmiScanner)
            scanKey = consumeRestoredStateForKey(PROVIDER)?.getString(SCAN_KEY)
        }

        sunmiScannerLauncher =
            activity.activityResultRegistry.register(
                CAMERA_SCANNER_KEY,
                owner,
                SunmiScannerActivityContract()
            ) { result ->
                result.map {
                    when (it) {
                        ScanEvent.Canceled -> scanEventProvider.addScanResult(it)
                        is ScanEvent.Success -> scanEventProvider.addScanResult(
                            it.copy(scanKey = scanKey)
                        )
                    }
                }
            }
        ContextCompat.registerReceiver(
            activity,
            broadcastReceiver,
            createIntentFilter(),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    override fun onDestroy(owner: LifecycleOwner) {
        activity.unregisterReceiver(broadcastReceiver)
        super.onDestroy(owner)
    }

    override fun saveState(): Bundle {
        return bundleOf(SCAN_KEY to scanKey)
    }

    override fun observeScannerResults(): SharedFlow<ScanEvent> = scanEventProvider.scanEvents

    override fun startCameraScanner(scanKey: String?) {
        this.scanKey = scanKey
        sunmiScannerLauncher.launch(scanIntent())
    }

    override fun scanNextWithKey(scanKey: String?) {
        this.scanKey = scanKey
    }

    sealed class ScanCodeType {
        data class Type(val code: String) : ScanCodeType()
        object Unknown : ScanCodeType()
    }

    private inner class SunmiBarcodeScannerBroadcastReceiver(private val scanEventProvider: ScanEventProvider) :
        BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val code = intent.getStringExtra(DATA)
            if (!code.isNullOrEmpty()) {
                Timber.v("scanned code: %s", code)
                scanEventProvider.addScanResult(ScanEvent.Success(code, scanKey))
            }
        }
    }

    companion object {
        private const val PROVIDER = "sunmi_scanner"
        private const val SCAN_KEY = "scan_key"
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

        private const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"

        private fun createIntentFilter(): IntentFilter = IntentFilter().apply {
            addAction(ACTION_DATA_CODE_RECEIVED)
        }
    }
}
