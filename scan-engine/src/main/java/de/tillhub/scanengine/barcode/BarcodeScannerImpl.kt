package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.ScanEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

abstract class BarcodeScannerImpl(protected val mutableScanEvents: MutableSharedFlow<ScanEvent>) : BarcodeScanner {

    protected var scanKey: String? = null

    override fun observeScannerResults(): Flow<ScanEvent> = mutableScanEvents

    override fun scanWithKey(scanKey: String?) {
        this.scanKey = scanKey
    }
}
