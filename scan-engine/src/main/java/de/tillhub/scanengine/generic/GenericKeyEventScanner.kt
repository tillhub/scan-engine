package de.tillhub.scanengine.generic

import android.app.Activity
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import de.tillhub.scanengine.KeyEventScanner
import de.tillhub.scanengine.barcode.BarcodeScannerImpl
import de.tillhub.scanengine.data.Scanner
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.ScannerResponse
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emptyFlow

internal class GenericKeyEventScanner(
    private val activity: Activity,
    mutableScannerEvents: MutableStateFlow<ScannerEvent>,
) : BarcodeScannerImpl(mutableScannerEvents), KeyEventScanner {

    private val inputBuffer = StringBuilder()
    private var lastEventTime: Long = 0L
    private val scanThreshold = 50L

    override fun startPairingScreen(scanner: ScannerType) = Unit
    override fun observeScanners(): Flow<List<Scanner>> = emptyFlow()
    override suspend fun disconnect(scannerId: String): ScannerResponse = ScannerResponse.Success.Disconnect
    override suspend fun connect(scannerId: String): ScannerResponse = ScannerResponse.Success.Connect

    override fun dispatchKeyEvent(event: KeyEvent, scanKey: String?) {
        if (event.action != KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) return

        val currentEventTime = event.eventTime
        val timeDiff = currentEventTime - lastEventTime

        if (lastEventTime == 0L || timeDiff >= scanThreshold) {
            inputBuffer.clear()

            val focusedView = activity.findViewById<View>(android.R.id.content).findFocus()
            val shouldHandleKeyEvent = when (focusedView) {
                is EditText -> !focusedView.hasFocus()
                else -> true
            }
            if (!shouldHandleKeyEvent) return
        }

        lastEventTime = currentEventTime

        event.unicodeChar.takeIf { it != 0 }?.toChar()?.let { inputBuffer.append(it) }

        if (event.keyCode == KeyEvent.KEYCODE_ENTER && inputBuffer.isNotEmpty()) {
            mutableScannerEvents.tryEmit(ScannerEvent.ScanResult(inputBuffer.toString(), scanKey))
            inputBuffer.clear()
            lastEventTime = 0L
        }
    }
}
