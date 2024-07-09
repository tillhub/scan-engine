package de.tillhub.scanengine

import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.Flow

interface BarcodeScanner {
    fun observeScannerResults(): Flow<ScanEvent>
    fun scanWithKey(scanKey: String? = null)
    fun startPairingScreen(scanner: ScannerType)
}
