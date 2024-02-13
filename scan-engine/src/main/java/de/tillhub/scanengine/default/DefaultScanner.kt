package de.tillhub.scanengine.default

import DefaultScannerActivityContract
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.VisibleForTesting
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScanEventProvider
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.Scanner.Companion.CAMERA_SCANNER_KEY
import kotlinx.coroutines.flow.SharedFlow

class DefaultScanner(
    private val activity: ComponentActivity,
    private val scanEventProvider: ScanEventProvider = ScanEventProvider(),
) : Scanner {

    @VisibleForTesting
    lateinit var defaultScannerLauncher: ActivityResultLauncher<String>

    private var scanKey: String? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        with(activity.savedStateRegistry) {
            registerSavedStateProvider(PROVIDER, this@DefaultScanner)
            scanKey = consumeRestoredStateForKey(PROVIDER)?.getString(SCAN_KEY)
        }

        defaultScannerLauncher =
            activity.activityResultRegistry.register(
                CAMERA_SCANNER_KEY,
                owner,
                DefaultScannerActivityContract()
            ) {
                when (it) {
                    ScanEvent.Canceled -> scanEventProvider.addScanResult(it)
                    is ScanEvent.Success -> scanEventProvider.addScanResult(
                        it.copy(scanKey = scanKey)
                    )
                }
            }
    }

    override fun saveState(): Bundle {
        return bundleOf(SCAN_KEY to scanKey)
    }
    override fun observeScannerResults(): SharedFlow<ScanEvent> = scanEventProvider.scanEvents

    override fun startCameraScanner(scanKey: String?) {
        this.scanKey = scanKey
        defaultScannerLauncher.launch(scanKey)
    }

    override fun scanNextWithKey(scanKey: String?) {
        this.scanKey = scanKey
    }

    companion object {
        private const val PROVIDER = "default_scanner"
        private const val SCAN_KEY = "scan_key"
    }
}
