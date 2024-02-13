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
import kotlinx.coroutines.flow.Flow

class DefaultScanner(
    private val activity: ComponentActivity
) : Scanner {

    @VisibleForTesting
    val scanEventProvider = ScanEventProvider()

    @VisibleForTesting
    lateinit var defaultScannerLauncher: ActivityResultLauncher<String>

    private var scanKey: String? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        with(activity.savedStateRegistry) {
            registerSavedStateProvider(PROVIDER, this@DefaultScanner)
            scanKey = consumeRestoredStateForKey(PROVIDER)?.getString(Scanner.SCAN_KEY)
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
        return bundleOf(Scanner.SCAN_KEY to scanKey)
    }

    override fun observeScannerResults(): Flow<ScanEvent> = scanEventProvider.scanEvents

    override fun startCameraScanner(scanKey: String?) {
        this.scanKey = scanKey
        defaultScannerLauncher.launch(scanKey)
    }

    override fun scanNextWithKey(scanKey: String?) {
        this.scanKey = scanKey
    }

    companion object {
        private const val PROVIDER = "default_scanner"
    }
}
