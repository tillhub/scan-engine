package de.tillhub.scanengine

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.savedstate.SavedStateRegistry
import kotlinx.coroutines.flow.SharedFlow

/**
 * Used for connecting and disconnecting a scanner, issue scan commands and observing scanned codes.
 */
interface Scanner : DefaultLifecycleObserver, SavedStateRegistry.SavedStateProvider {

    /**
     * Can be used to observe any scanned code.
     */
    fun observeScannerResults(): SharedFlow<ScanEvent>

    /**
     * Start the camera based scanner.
     */
    fun startCameraScanner(scanKey: String? = null)

    /**
     * Scan with next key
     */
    fun scanNextWithKey(scanKey: String?)

    companion object {
        const val CAMERA_SCANNER_KEY = "CAMERA_SCANNER_KEY"
    }
}
