package de.tillhub.scanengine.barcode

import android.content.Context
import de.tillhub.scanengine.barcode.zebra.ZebraBarcodeScanner
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerManufacturer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge

internal class BarcodeScannerContainer(
    context: Context,
    private val mutableScanEvents: MutableStateFlow<ScanEvent>
) : BarcodeScanner {

    private val barcodeScanners = mutableListOf<BarcodeScanner>().apply {
        val barcodeScanner = when (ScannerManufacturer.get()) {
            ScannerManufacturer.SUNMI -> SunmiBarcodeScanner(context, mutableScanEvents)
            ScannerManufacturer.NOT_SUNMI -> DefaultBarcodeScanner(mutableScanEvents)
        }
        add(barcodeScanner)

        val zebraScanner = ZebraBarcodeScanner(context, mutableScanEvents)
        add(zebraScanner)
    }

    fun getScannersByType(type: Class<out BarcodeScanner>): BarcodeScanner {
        return barcodeScanners.find { it::class.java == type }
            ?: throw NoSuchElementException("No scanner found of type $type")
    }

    override fun observeScannerResults(): Flow<ScanEvent> {
        return barcodeScanners
            .map { it.observeScannerResults() }
            .fold(emptyFlow()) { accumulator, flow -> merge(accumulator, flow) }
    }

    override fun scanWithKey(scanKey: String?) {
        barcodeScanners.forEach {
            it.scanWithKey(scanKey)
        }
    }

    override fun initScanner() {
        barcodeScanners.forEach {
            it.initScanner()
        }
    }
}
