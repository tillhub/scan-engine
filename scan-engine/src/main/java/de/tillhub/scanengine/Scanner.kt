package de.tillhub.scanengine

import kotlinx.coroutines.flow.StateFlow

interface Scanner {
    fun observeScannerResults(): StateFlow<ScanEvent>

    fun startCameraScanner(scanKey: String? = null)
}
