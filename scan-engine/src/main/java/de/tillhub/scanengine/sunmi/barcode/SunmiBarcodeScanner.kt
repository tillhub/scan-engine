package de.tillhub.scanengine.sunmi.barcode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import de.tillhub.scanengine.barcode.BarcodeScannerImpl
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.Scanner
import de.tillhub.scanengine.data.ScannerResponse
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import timber.log.Timber

internal class SunmiBarcodeScanner(
    context: Context,
    mutableScanEvents: MutableSharedFlow<ScanEvent>
) : BarcodeScannerImpl(mutableScanEvents) {

    private val broadcastReceiver = SunmiBarcodeScannerBroadcastReceiver()

    init {
        ContextCompat.registerReceiver(
            context,
            broadcastReceiver,
            createIntentFilter(),
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun createIntentFilter(): IntentFilter = IntentFilter().apply {
        addAction(ACTION_DATA_CODE_RECEIVED)
    }

    private inner class SunmiBarcodeScannerBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val code = intent.getStringExtra(DATA)
            if (!code.isNullOrEmpty()) {
                Timber.v("scanned code: %s", code)
                mutableScanEvents.tryEmit(ScanEvent.Success(code, scanKey))
            }
        }
    }

    companion object {
        private const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"
        private const val DATA = "data"
    }

    override fun startPairingScreen(scanner: ScannerType) = Unit
    override fun observeScanners(): Flow<List<Scanner>> = emptyFlow()
    override suspend fun connect(scannerId: String): ScannerResponse = ScannerResponse.NotFound
    override fun disconnect(scannerId: String) = Unit
}
