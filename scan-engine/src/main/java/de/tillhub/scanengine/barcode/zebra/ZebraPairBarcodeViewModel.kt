package de.tillhub.scanengine.barcode.zebra

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zebra.scannercontrol.IDcsSdkApi
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.barcode.BarcodeScannerContainer
import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal class ZebraPairBarcodeViewModel(scanEngine: ScanEngine) : ViewModel() {

    private val zebraBarcodeScanner =
        (scanEngine.barcodeScanner as BarcodeScannerContainer).getScannersByType(
            ZebraBarcodeScanner::class.java
        ) as ZebraBarcodeScanner

    val isConnected: StateFlow<Boolean> = scanEngine.observeScannerResults().map { it is ScanEvent.Connected }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = false
    )

    fun initScanner() {
        zebraBarcodeScanner.initScanner()
    }

    fun getSdkHandler(): IDcsSdkApi = zebraBarcodeScanner.sdkHandler

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val scanEngine by lazy { ScanEngine.getInstance(context) }
                ZebraPairBarcodeViewModel(scanEngine)
            }
        }
    }
}
