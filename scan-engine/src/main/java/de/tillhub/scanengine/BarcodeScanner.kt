package de.tillhub.scanengine

import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.Scanner
import de.tillhub.scanengine.data.ScannerResponse
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.Flow

interface BarcodeScanner {
    fun observeScannerResults(): Flow<ScanEvent>
    fun scanWithKey(scanKey: String? = null)
    fun startPairingScreen(scanner: ScannerType)
    fun observeScanners(): Flow<List<Scanner>>
    suspend fun disconnect(scannerId: String): ScannerResponse
    suspend fun connect(scannerId: String): ScannerResponse
}
