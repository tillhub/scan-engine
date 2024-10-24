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

    override fun startPairingScreen(scanner: ScannerType) = Unit
    override fun observeScanners(): Flow<List<Scanner>> = emptyFlow()
    override suspend fun disconnect(scannerId: String): ScannerResponse = ScannerResponse.Success.Disconnect
    override suspend fun connect(scannerId: String): ScannerResponse = ScannerResponse.Success.Connect

    /* dispatchKeyEvent processes key events by appending characters to the input buffer.
       It resets the buffer if the time between events exceeds SCAN_THRESHOLD.
       When Enter is pressed, it emits the scanned result and clears the buffer. */
    override fun dispatchKeyEvent(event: KeyEvent, scanKey: String?) {
        if (event.action != KeyEvent.ACTION_DOWN || event.keyCode == KeyEvent.KEYCODE_SHIFT_LEFT) return

        val currentEventTime = event.eventTime
        val timeDiff = currentEventTime - lastEventTime

        if (lastEventTime == 0L || timeDiff >= SCAN_THRESHOLD) {
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

    companion object {
        /* SCAN_THRESHOLD is the minimum time (in ms) between key events to distinguish scans.
        If the time exceeds this value, the input buffer resets for a new scan.*/
        private const val SCAN_THRESHOLD = 50L
    }
}
