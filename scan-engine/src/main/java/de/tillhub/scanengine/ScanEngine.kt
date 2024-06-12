package de.tillhub.scanengine

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultCaller
import de.tillhub.scanengine.barcode.BarcodeScanner
import de.tillhub.scanengine.barcode.BarcodeScannerContainer
import de.tillhub.scanengine.barcode.zebra.ZebraPairBarcodeActivity
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerManufacturer
import de.tillhub.scanengine.google.DefaultCameraScanner
import de.tillhub.scanengine.helper.SingletonHolder
import de.tillhub.scanengine.scanner.sunmi.SunmiCameraScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

class ScanEngine private constructor(private val context: Context) {

    private val mutableScanEvents = MutableStateFlow<ScanEvent>(ScanEvent.NotConnected)

    fun observeScannerResults(): Flow<ScanEvent> = mutableScanEvents.drop(1)

    fun newCameraScanner(
        resultCaller: ActivityResultCaller,
    ): CameraScanner {
        return when (ScannerManufacturer.get()) {
            ScannerManufacturer.SUNMI -> SunmiCameraScanner(
                resultCaller,
                mutableScanEvents
            )

            ScannerManufacturer.NOT_SUNMI -> DefaultCameraScanner(
                resultCaller,
                mutableScanEvents
            )
        }
    }

    val barcodeScanner: BarcodeScanner = BarcodeScannerContainer(context, mutableScanEvents)

    fun startZebraPairBarcodeActivity() {
        context.startActivity(
            Intent(context, ZebraPairBarcodeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }

    companion object : SingletonHolder<ScanEngine, Context>(::ScanEngine)
}
