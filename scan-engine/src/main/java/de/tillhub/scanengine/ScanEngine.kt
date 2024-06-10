package de.tillhub.scanengine

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import de.tillhub.scanengine.barcode.BarcodeScanner
import de.tillhub.scanengine.barcode.DefaultBarcodeScanner
import de.tillhub.scanengine.barcode.SunmiBarcodeScanner
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerManufacturer
import de.tillhub.scanengine.default.DefaultScanner
import de.tillhub.scanengine.helper.SingletonHolder
import de.tillhub.scanengine.sunmi.SunmiScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

class ScanEngine private constructor(context: Context) {

    private val mutableScanEvents = MutableStateFlow<ScanEvent>(ScanEvent.Idle)
    fun observeScannerResults(): Flow<ScanEvent> = mutableScanEvents.drop(1)

    fun newCameraScanner(
        resultCaller: ActivityResultCaller,
    ): Scanner {
        return when (ScannerManufacturer.get()) {
            ScannerManufacturer.SUNMI -> SunmiScanner(
                resultCaller,
                mutableScanEvents
            )

            ScannerManufacturer.OTHER -> DefaultScanner(
                resultCaller,
                mutableScanEvents
            )
        }
    }

    val barcodeScanner: BarcodeScanner = when (ScannerManufacturer.get()) {
        ScannerManufacturer.SUNMI -> SunmiBarcodeScanner(context, mutableScanEvents)
        ScannerManufacturer.OTHER -> DefaultBarcodeScanner(mutableScanEvents)
    }

    companion object : SingletonHolder<ScanEngine, Context>(::ScanEngine)
}
