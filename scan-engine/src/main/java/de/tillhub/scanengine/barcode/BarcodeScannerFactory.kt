package de.tillhub.scanengine.barcode

import android.content.Context
import de.tillhub.scanengine.BarcodeScanner
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.sunmi.barcode.SunmiBarcodeScanner
import de.tillhub.scanengine.zebra.ZebraBarcodeScanner
import kotlinx.coroutines.flow.MutableStateFlow

internal class BarcodeScannerFactory {

    fun getSunmiBarcodeScanner(
        context: Context,
        scanEvents: MutableStateFlow<ScanEvent>
    ): BarcodeScanner = SunmiBarcodeScanner(context, scanEvents)

    fun getZebraBarcodeScanner(
        context: Context,
        scanEvents: MutableStateFlow<ScanEvent>
    ): BarcodeScanner = ZebraBarcodeScanner(context, scanEvents)
}
