package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.ScanEvent
import kotlinx.coroutines.flow.Flow

interface BarcodeScanner {
    fun observeScannerResults(): Flow<ScanEvent>
    fun scanWithKey(scanKey: String? = null)
    fun unregisterBroadcastReceiver()
}
