package de.tillhub.scanengine

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.savedstate.SavedStateRegistry
import kotlinx.coroutines.flow.SharedFlow

interface Scanner : DefaultLifecycleObserver, SavedStateRegistry.SavedStateProvider {
    fun observeScannerResults(): SharedFlow<ScanEvent>
    fun startCameraScanner(scanKey: String? = null)

    companion object {
        const val PROVIDER = "scanner"
        const val SCAN_KEY = "scan_key"
        const val CAMERA_SCANNER_KEY = "CAMERA_SCANNER_KEY"
    }
}
