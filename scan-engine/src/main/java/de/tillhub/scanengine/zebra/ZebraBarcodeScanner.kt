package de.tillhub.scanengine.zebra

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsScannerEventsOnReLaunch
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler
import de.tillhub.scanengine.barcode.BarcodeScannerImpl
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.MutableStateFlow

internal class ZebraBarcodeScanner(
    private val context: Context,
    events: MutableStateFlow<ScanEvent>
) : BarcodeScannerImpl(events), IDcsSdkApiDelegate, IDcsScannerEventsOnReLaunch {

    private var scannerID: Int? = null
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val sdkHandler: SDKHandler by lazy {
        SDKHandler(context, true).apply {

            dcssdkSetDelegate(this@ZebraBarcodeScanner)
            dcssdkEnableAvailableScannersDetection(true)
            setiDcsScannerEventsOnReLaunch(this@ZebraBarcodeScanner)

            // Bluetooth classic mode.
            dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_NORMAL)

            // Bluetooth low energy mode.
            dcssdkSetOperationalMode(DCSSDKDefs.DCSSDK_MODE.DCSSDK_OPMODE_BT_LE)

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
        }
    }

    init {
        initScanner()
    }

    fun initScanner(): Result<SDKHandler> {
        val btEnabled = bluetoothManager.adapter.isEnabled
        return if (hasPermissions() && btEnabled) {
            try {
                Result.success(sdkHandler).also {
                    printAvailableScanners()
                }
            } catch (e: SecurityException) {
                Result.failure(e)
            }
        } else {
            Result.failure(
                IllegalStateException(
                    "SDKHandler initialization failed. Has permissions: ${hasPermissions()}, BT enabled: $btEnabled"
                )
            )
        }
    }

    private fun printAvailableScanners() {
        val scannerTreeList = arrayListOf<DCSScannerInfo>()
        sdkHandler.dcssdkGetAvailableScannersList(scannerTreeList)
        sdkHandler.dcssdkGetActiveScannersList(scannerTreeList)
        scannerTreeList.onEach {
            Log.d("=========", "===========${it.scannerID}")
        }
    }

    override fun startPairingScreen(scanner: ScannerType) {
        require(scanner == ScannerType.ZEBRA) {
            "startPairingScreen: Scanner type must be Zebra but it is: ${scanner.value}"
        }
        val intent = Intent(context, ZebraPairBarcodeActivity::class.java)
        if (context !is Activity) {
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    override fun dcssdkEventCommunicationSessionEstablished(scanner: DCSScannerInfo?) {
        if (scanner == null) return
        scannerID = scanner.scannerID
        sdkHandler.dcssdkTerminateCommunicationSession(scanner.scannerID)
        printAvailableScanners()
        mutableScanEvents.tryEmit(ScanEvent.Connected)
    }

    override fun dcssdkEventCommunicationSessionTerminated(scannerId: Int) = Unit

    override fun dcssdkEventBarcode(barcodeData: ByteArray?, barcodeType: Int, scannerId: Int) {
        barcodeData?.let {
            mutableScanEvents.tryEmit(ScanEvent.Success(String(it, Charsets.ISO_8859_1)))
        }
    }

    override fun dcssdkEventScannerAppeared(scanner: DCSScannerInfo?) = Unit
    override fun dcssdkEventScannerDisappeared(p0: Int) = Unit
    override fun dcssdkEventImage(p0: ByteArray?, p1: Int) = Unit
    override fun dcssdkEventVideo(p0: ByteArray?, p1: Int) = Unit
    override fun dcssdkEventBinaryData(p0: ByteArray?, p1: Int) = Unit
    override fun dcssdkEventFirmwareUpdate(p0: FirmwareUpdateEvent?) = Unit
    override fun dcssdkEventAuxScannerAppeared(p0: DCSScannerInfo?, p1: DCSScannerInfo?) = Unit
    override fun dcssdkEventConfigurationUpdate(p0: ConfigurationUpdateEvent?) = Unit

    override fun onLastConnectedScannerDetect(device: BluetoothDevice?): Boolean = true
    override fun onConnectingToLastConnectedScanner(device: BluetoothDevice?) = Unit
    override fun onScannerDisconnect() = Unit

    private fun hasPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS
            .map { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            .all { it }
    }

    fun connectDiscoverableMode(): Boolean {
      printAvailableScanners()
        return scannerID?.let {
            sdkHandler.dcssdkEstablishCommunicationSession(it)
            val result = sdkHandler.dcssdkEstablishCommunicationSession(it)
            result == DCSSDKDefs.DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS
        } ?: false
    }

    companion object {
        private const val HW_SERIAL_NUMBER: String = "hwSerialNumber"
        private const val PREFERENCES_FILE: String = "Prefs"
        val BLUETOOTH_PERMISSIONS = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )

            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> listOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_ADVERTISE
            )

            else -> listOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        }
    }
}
