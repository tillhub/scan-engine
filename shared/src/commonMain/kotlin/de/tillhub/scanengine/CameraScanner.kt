package de.tillhub.scanengine

import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.StateFlow

interface CameraScanner {
    fun observeScannerResults(): StateFlow<ScannerEvent>
    fun startCameraScanner(scanKey: String? = null)
}
