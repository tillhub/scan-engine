package de.tillhub.scanengine.barcode

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import de.tillhub.scanengine.ScanEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import timber.log.Timber

class SunmiBarcodeScanner(
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
        const val DATA = "data"
    }
}
