package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DefaultBarcodeScanner(
    mutableScanEvents: MutableSharedFlow<ScanEvent>
) : BarcodeScannerImpl(mutableScanEvents) {
    override fun initScanner() = Unit
}
