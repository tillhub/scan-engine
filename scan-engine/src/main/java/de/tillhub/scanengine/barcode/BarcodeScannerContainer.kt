package de.tillhub.scanengine.barcode

import android.content.Context
import de.tillhub.scanengine.BarcodeScanner
import de.tillhub.scanengine.zebra.ZebraBarcodeScanner
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.Scanner
import de.tillhub.scanengine.data.ScannerResponse
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.sunmi.barcode.SunmiBarcodeScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge

internal class BarcodeScannerContainer(
    context: Context,
    mutableScannerEvents: MutableStateFlow<ScannerEvent>,
    externalScanners: List<ScannerType>,
    private val scannerFactory: BarcodeScannerFactory = BarcodeScannerFactory()
) : BarcodeScanner {

    private val barcodeScanners = mutableListOf<BarcodeScanner>()

    init {
        barcodeScanners.apply {
            when (ScannerType.get()) {
                ScannerType.SUNMI -> {
                    add(scannerFactory.getSunmiBarcodeScanner(context, mutableScannerEvents))
                }

                else -> Unit
            }

            externalScanners.distinct().forEach {
                when (it) {
                    ScannerType.ZEBRA -> {
                        add(scannerFactory.getZebraBarcodeScanner(context, mutableScannerEvents))
                    }

                    ScannerType.SUNMI,
                    ScannerType.UNKNOWN -> Unit
                }
            }
        }
    }

    fun getScannersByType(type: Class<out BarcodeScanner>): BarcodeScanner {
        return barcodeScanners.find { it::class.java == type }
            ?: throw NoSuchElementException("No scanner found of type $type")
    }

    override fun observeScannerResults(): Flow<ScannerEvent> {
        return barcodeScanners.map { it.observeScannerResults() }
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

    override fun observeScanners(): Flow<List<Scanner>> {
        return barcodeScanners.map { it.observeScanners() }
            .fold(emptyFlow()) { accumulator, flow -> merge(accumulator, flow) }
    }

    override suspend fun connect(scannerId: String): ScannerResponse {
        return barcodeScanners
            .map { it.connect(scannerId) }
            .firstOrNull { it is ScannerResponse.Success || it is ScannerResponse.Error.Connect }
            ?: ScannerResponse.Error.NotFound
    }

    override suspend fun disconnect(scannerId: String): ScannerResponse {
        return barcodeScanners.map { it.disconnect(scannerId) }
            .firstOrNull { it is ScannerResponse.Success || it is ScannerResponse.Error.Disconnect }
            ?: ScannerResponse.Error.NotFound
    }
}
