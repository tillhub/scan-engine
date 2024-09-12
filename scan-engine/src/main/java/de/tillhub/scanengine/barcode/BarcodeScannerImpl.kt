package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.BarcodeScanner
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal abstract class BarcodeScannerImpl(
    protected val mutableScannerEvents: MutableSharedFlow<ScannerEvent>
) : BarcodeScanner {

    protected var scanKey: String? = null

    override fun observeScannerResults(): Flow<ScannerEvent> = mutableScannerEvents

    override fun scanWithKey(scanKey: String?) {
        this.scanKey = scanKey
    }
}
