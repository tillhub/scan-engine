package de.tillhub.scanengine

import kotlinx.coroutines.flow.SharedFlow

interface Scanner {
    fun observeScannerResults(): SharedFlow<ScanEvent>

    fun startCameraScanner(scanKey: String? = null)
}
