package de.tillhub.scanengine.default

import DefaultScannerActivityContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.LifecycleOwner
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScanEventProvider
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.Scanner.Companion.CAMERA_SCANNER_KEY
import kotlinx.coroutines.flow.Flow

class DefaultScanner(
    private val registry: ActivityResultRegistry,
) : Scanner {

    @VisibleForTesting
    val scanEventProvider = ScanEventProvider()

    @VisibleForTesting
    lateinit var defaultScannerLauncher: ActivityResultLauncher<String>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        defaultScannerLauncher =
            registry.register(
                CAMERA_SCANNER_KEY,
                owner,
                DefaultScannerActivityContract()
            ) { result ->
                scanEventProvider.addScanResult(result)
            }
    }

    override fun observeScannerResults(): Flow<ScanEvent> = scanEventProvider.scanEvents

    override fun startCameraScanner(scanKey: String?) {
        defaultScannerLauncher.launch(scanKey)
    }

    override fun scanNextWithKey(scanKey: String?) {
        // Will be implemented when Bluetooth/Laser scanner gets implemented
    }
}
