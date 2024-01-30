package de.tillhub.scanengine

import kotlinx.coroutines.flow.Flow

/**
 * Used for connecting and disconnecting a scanner, issue scan commands and observing scanned codes.
 */
interface Scanner {
    /**
     * Can be used to observe any scanned code.
     */
    fun observeScannerResults(): Flow<ScanEvent>

    /**
     * Start the camera based scanner.
     */
    fun scanCameraCode(scanKey: String?)

    fun scanNextWithKey(scanKey: String)
}
