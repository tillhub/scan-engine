package de.tillhub.scanengine

import android.content.Context
import androidx.activity.result.ActivityResultCaller
import de.tillhub.scanengine.barcode.BarcodeScannerContainer
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.google.DefaultCameraScanner
import de.tillhub.scanengine.common.SingletonHolder
import de.tillhub.scanengine.sunmi.camera.SunmiCameraScanner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

class ScanEngine private constructor(private val context: Context) {

    private val mutableScanEvents = MutableStateFlow<ScanEvent>(ScanEvent.NotConnected)

    fun observeScannerResults(): Flow<ScanEvent> = mutableScanEvents.drop(1)

    fun newCameraScanner(
        resultCaller: ActivityResultCaller,
    ): CameraScanner {
        return when (ScannerType.get()) {
            ScannerType.SUNMI -> SunmiCameraScanner(resultCaller, mutableScanEvents)
            else -> DefaultCameraScanner(resultCaller, mutableScanEvents)
        }
    }

    private var externalScanners: List<ScannerType> = emptyList()
    fun initBarcodeScanners(vararg externalScanners: ScannerType): ScanEngine {
        this.externalScanners = externalScanners.asList()
        return this
    }

    val barcodeScanner: BarcodeScanner by lazy {
        BarcodeScannerContainer(context, mutableScanEvents, externalScanners)
    }

    companion object : SingletonHolder<ScanEngine, Context>(::ScanEngine)
}
