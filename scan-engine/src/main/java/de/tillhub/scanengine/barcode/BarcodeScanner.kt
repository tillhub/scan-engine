package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.Flow

interface BarcodeScanner {
    fun observeScannerResults(): Flow<ScanEvent>
    fun scanWithKey(scanKey: String? = null)
}
