package de.tillhub.scanengine.default

import DefaultScannerActivityContract
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.ScannerImpl
import kotlinx.coroutines.flow.MutableSharedFlow

class DefaultScanner(
    private val activityResultRegistry: ActivityResultRegistry,
    savedStateRegistry: SavedStateRegistry,
    mutableScanEvents: MutableSharedFlow<ScanEvent>,
) : ScannerImpl(savedStateRegistry, mutableScanEvents) {

    private lateinit var scannerLauncher: ActivityResultLauncher<String>

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        scannerLauncher = activityResultRegistry.register(
            Scanner.CAMERA_SCANNER_KEY,
            owner,
            DefaultScannerActivityContract()
        ) {
            when (it) {
                ScanEvent.Canceled -> mutableScanEvents.tryEmit(it)
                is ScanEvent.Success -> mutableScanEvents.tryEmit(
                    it.copy(scanKey = scanKey)
                )
            }
        }
    }

    override fun startCameraScanner(scanKey: String?) {
        this.scanKey = scanKey
        scannerLauncher.launch(scanKey)
    }
}
