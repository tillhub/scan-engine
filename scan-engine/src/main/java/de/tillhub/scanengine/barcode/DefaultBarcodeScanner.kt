package de.tillhub.scanengine.barcode

import android.content.Context
import de.tillhub.scanengine.ScanEvent
import kotlinx.coroutines.flow.MutableSharedFlow

class DefaultBarcodeScanner(
    context: Context,
    mutableScanEvents: MutableSharedFlow<ScanEvent>
) : BarcodeScannerImpl(mutableScanEvents)
