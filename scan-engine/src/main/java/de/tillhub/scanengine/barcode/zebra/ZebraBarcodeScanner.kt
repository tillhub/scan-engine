package de.tillhub.scanengine.barcode.zebra

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsScannerEventsOnReLaunch
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler
import de.tillhub.scanengine.barcode.BarcodeScannerImpl
import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.flow.MutableStateFlow

internal class ZebraBarcodeScanner(
    private val context: Context,
    events: MutableStateFlow<ScanEvent>
) : BarcodeScannerImpl(events), IDcsScannerEventsOnReLaunch, IDcsSdkApiDelegate {

    val sdkHandler: SDKHandler by lazy {
        SDKHandler(context, true).apply {
            dcssdkSetDelegate(this@ZebraBarcodeScanner)
            dcssdkEnableAvailableScannersDetection(true)
            setiDcsScannerEventsOnReLaunch(this@ZebraBarcodeScanner)
            dcssdkSubsribeForEvents(
                // We would like to subscribe to all barcode events
                DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_BARCODE.value

                    // We would like to subscribe to all scanner available/not-available events
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_APPEARANCE.value
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SCANNER_DISAPPEARANCE.value

                    // We would like to subscribe to all scanner connection events
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_ESTABLISHMENT.value
                    or DCSSDKDefs.DCSSDK_EVENT.DCSSDK_EVENT_SESSION_TERMINATION.value
            )
            // Bluetooth classic mode.
            dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)
            // Bluetooth low energy mode.
            dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)
        }
    }
    private var barcodeScanner by mutableStateOf<DCSScannerInfo?>(null)
    private var reconnecting = false

    init {
        initScanner()
    }

    override fun initScanner() {
        if (hasPermissions()) {
            sdkHandler
        }
    }

    override fun onLastConnectedScannerDetect(device: BluetoothDevice): Boolean {
        return true
    }

    override fun onConnectingToLastConnectedScanner(device: BluetoothDevice) {
        reconnecting = true
    }

    override fun onScannerDisconnect() {
        barcodeScanner = null
    }

    override fun dcssdkEventCommunicationSessionEstablished(scanner: DCSScannerInfo?) {
        if (scanner == null) return
        barcodeScanner = scanner
        sdkHandler.dcssdkEnableAutomaticSessionReestablishment(true, scanner.scannerID)
        context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE).edit().apply {
            putString(HW_SERIAL_NUMBER, scanner.scannerHWSerialNumber)
            apply()
        }
        mutableScanEvents.tryEmit(ScanEvent.Connected)
    }

    override fun dcssdkEventCommunicationSessionTerminated(scannerId: Int) {
        if (scannerId == barcodeScanner?.scannerID) {
            barcodeScanner = null
        }
    }

    override fun dcssdkEventBarcode(
        barcodeData: ByteArray?,
        barcodeType: Int,
        scannerId: Int
    ) {
        barcodeData?.let {
            mutableScanEvents.tryEmit(ScanEvent.Success(String(it, Charsets.ISO_8859_1)))
        }
    }
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventScannerAppeared(p0: DCSScannerInfo?) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventScannerDisappeared(p0: Int) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventImage(p0: ByteArray?, p1: Int) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) {}
    @Suppress("EmptyFunctionBlock")
    override fun dcssdkEventConfigurationUpdate(p0: ConfigurationUpdateEvent?) {}

    private fun hasPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS
            .map { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            .reduce { acc, b -> acc && b }
    }

    companion object {
        val BLUETOOTH_PERMISSIONS = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
            )
        } else {
            listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
        const val HW_SERIAL_NUMBER: String = "hwSerialNumber"
        const val PREFERENCES_FILE: String = "Prefs"
    }
}
