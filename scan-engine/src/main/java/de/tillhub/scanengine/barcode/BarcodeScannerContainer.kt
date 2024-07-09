package de.tillhub.scanengine.barcode

import android.content.Context
import de.tillhub.scanengine.BarcodeScanner
import de.tillhub.scanengine.zebra.ZebraBarcodeScanner
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.sunmi.barcode.SunmiBarcodeScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge

internal class BarcodeScannerContainer(
    context: Context,
    mutableScanEvents: MutableStateFlow<ScanEvent>,
    externalScanners: List<ScannerType>
) : BarcodeScanner {

    private val barcodeScanners = mutableListOf<BarcodeScanner>().apply {
        when (ScannerType.get()) {
            ScannerType.SUNMI -> {
                add(SunmiBarcodeScanner(context, mutableScanEvents))
            }
            else -> Unit
        }
        externalScanners.distinct().forEach {
            when (it) {
                ScannerType.ZEBRA -> {
                    add(ZebraBarcodeScanner(context, mutableScanEvents))
                }
                ScannerType.SUNMI,
                ScannerType.UNKNOWN -> Unit
            }
        }
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

    override fun startPairingScreen(scanner: ScannerType) {
        when (scanner) {
            ScannerType.ZEBRA -> getScannersByType(ZebraBarcodeScanner::class.java).startPairingScreen(scanner)
            ScannerType.SUNMI -> getScannersByType(SunmiBarcodeScanner::class.java).startPairingScreen(scanner)
            ScannerType.UNKNOWN -> Unit
        }
    }
}