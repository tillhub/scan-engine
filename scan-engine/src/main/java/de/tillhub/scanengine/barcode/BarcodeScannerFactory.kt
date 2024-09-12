package de.tillhub.scanengine.barcode

import android.content.Context
import de.tillhub.scanengine.BarcodeScanner
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.sunmi.barcode.SunmiBarcodeScanner
import de.tillhub.scanengine.zebra.ZebraBarcodeScanner
import kotlinx.coroutines.flow.MutableStateFlow

internal class BarcodeScannerFactory {

    fun getSunmiBarcodeScanner(
        context: Context,
        scannerEvents: MutableStateFlow<ScannerEvent>
    ): BarcodeScanner = SunmiBarcodeScanner(context, scannerEvents)

    fun getZebraBarcodeScanner(
        context: Context,
        scannerEvents: MutableStateFlow<ScannerEvent>
    ): BarcodeScanner = ZebraBarcodeScanner(context, scannerEvents)
}
