package de.tillhub.scanengine

import SingletonHolder
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import de.tillhub.scanengine.barcode.BarcodeScanner
import de.tillhub.scanengine.barcode.DefaultBarcodeScanner
import de.tillhub.scanengine.barcode.SunmiBarcodeScanner
import de.tillhub.scanengine.default.DefaultScanner
import de.tillhub.scanengine.helper.ManagerBuilder
import de.tillhub.scanengine.sunmi.SunmiScanner
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class ScanEngine private constructor(context: Context) {
    private val mutableScanEvents = MutableSharedFlow<ScanEvent>(extraBufferCapacity = 1)
    fun observeScannerResults(): SharedFlow<ScanEvent> = mutableScanEvents
    fun newCameraScanner(activity: ComponentActivity): ManagerBuilder<Scanner> {
        return object : ManagerBuilder<Scanner> {
            override fun build(lifecycle: Lifecycle): Scanner {
                return when (ScannerManufacturer.get()) {
                    ScannerManufacturer.SUNMI -> SunmiScanner(
                        activity.activityResultRegistry,
                        activity.savedStateRegistry,
                        mutableScanEvents
                    )

                    ScannerManufacturer.OTHER -> DefaultScanner(
                        activity.activityResultRegistry,
                        activity.savedStateRegistry,
                        mutableScanEvents
                    )
                }.also {
                    lifecycle.addObserver(it)
                }
            }
        }
    }

    val barcodeScanner: BarcodeScanner by lazy {
        when (ScannerManufacturer.get()) {
            ScannerManufacturer.SUNMI -> SunmiBarcodeScanner(context, mutableScanEvents)
            ScannerManufacturer.OTHER -> {
                DefaultBarcodeScanner(context, mutableScanEvents)
            }
        }
    }

    companion object : SingletonHolder<ScanEngine, Context>(::ScanEngine)
}
