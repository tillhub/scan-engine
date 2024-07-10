package de.tillhub.scanengine.zebra

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.zebra.scannercontrol.SDKHandler
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.barcode.BarcodeScannerContainer
import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

internal class ZebraPairBarcodeViewModel(scanEngine: ScanEngine) : ViewModel() {

    private val uiState: MutableStateFlow<State> = MutableStateFlow(State.Loading)
    val uiStateFlow: StateFlow<State> get() = uiState

    private val zebraBarcodeScanner =
        (scanEngine.barcodeScanner as BarcodeScannerContainer).getScannersByType(
            ZebraBarcodeScanner::class.java
        ) as ZebraBarcodeScanner

    init {
        viewModelScope.launch {
            scanEngine.observeScannerResults().collect { event ->
                if (event is ScanEvent.Connected) {
                    uiState.value = State.Connected
                }
            }
        }
    }

    fun initScanner() {
        uiState.value = State.Pairing(zebraBarcodeScanner.initScanner())
    }

    sealed class State {
        data object Loading : State()
        data class Pairing(val result: Result<SDKHandler>) : State()
        data object Connected : State()
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ZebraPairBarcodeViewModel(ScanEngine.getInstance(context))
            }
        }
    }
}
