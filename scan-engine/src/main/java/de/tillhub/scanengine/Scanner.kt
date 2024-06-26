package de.tillhub.scanengine

import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.StateFlow

interface Scanner {
    fun observeScannerResults(): StateFlow<ScanEvent>

    fun startCameraScanner(scanKey: String? = null)
}
