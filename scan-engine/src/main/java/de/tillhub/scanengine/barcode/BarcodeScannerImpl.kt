package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.BarcodeScanner
import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal abstract class BarcodeScannerImpl(
    protected val mutableScanEvents: MutableSharedFlow<ScanEvent>
) : BarcodeScanner {

    protected var scanKey: String? = null

    override fun observeScannerResults(): Flow<ScanEvent> = mutableScanEvents

    override fun scanWithKey(scanKey: String?) {
        this.scanKey = scanKey
    }
}
