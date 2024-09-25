package de.tillhub.scanengine.zebra

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothDevice.ACTION_BOND_STATE_CHANGED
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.registerReceiver
import com.zebra.barcode.sdk.sms.ConfigurationUpdateEvent
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.DCSSDKDefs.DCSSDK_RESULT
import com.zebra.scannercontrol.DCSScannerInfo
import com.zebra.scannercontrol.FirmwareUpdateEvent
import com.zebra.scannercontrol.IDcsScannerEventsOnReLaunch
import com.zebra.scannercontrol.IDcsSdkApiDelegate
import com.zebra.scannercontrol.SDKHandler
import de.tillhub.scanengine.R
import de.tillhub.scanengine.barcode.BarcodeScannerImpl
import de.tillhub.scanengine.data.Scanner
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.ScannerResponse
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class ZebraBarcodeScanner(
    private val context: Context,
    events: MutableStateFlow<ScannerEvent>
) : BarcodeScannerImpl(events), IDcsSdkApiDelegate, IDcsScannerEventsOnReLaunch {

    private val availableScannersFlow by lazy {
        MutableStateFlow(emptyList<Scanner>())
    }
    private val bluetoothManager: BluetoothManager by lazy {
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }
    private val broadcastReceiver = ZebraBarcodeBroadcastReceiver()
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
            registerReceiver(
                context,
                broadcastReceiver,
                createIntentFilter(),
                ContextCompat.RECEIVER_EXPORTED
            )
            try {
                Result.success(sdkHandler)
            } catch (e: SecurityException) {
                Result.failure(e)
            }.also {
                availableScannersFlow.value = fetchScanners()
            }
        } else {
            Result.failure(
                IllegalStateException(
                    "SDKHandler initialization failed. Has permissions: ${hasPermissions()}, BT enabled: $btEnabled"
                )
            )
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

    override fun observeScanners(): Flow<List<Scanner>> = availableScannersFlow

    override suspend fun connect(scannerId: String): ScannerResponse {
        val result = try {
            mutableScannerEvents.tryEmit(ScannerEvent.External.Connecting(scannerId = scannerId))
            sdkHandler.dcssdkEstablishCommunicationSession(scannerId.toInt())
        } catch (e: NumberFormatException) {
            return ScannerResponse.Error.NotFound
        }
        return when (result) {
            DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS -> {
                mutableScannerEvents.tryEmit(ScannerEvent.External.Connected)
                ScannerResponse.Success.Connect
            }

            DCSSDK_RESULT.DCSSDK_RESULT_FAILURE ->
                ScannerResponse.Error.Connect(R.drawable.classic_discoverable)

            else -> ScannerResponse.Error.NotFound
        }
    }

    override suspend fun disconnect(scannerId: String): ScannerResponse {
        val result = try {
            sdkHandler.dcssdkTerminateCommunicationSession(scannerId.toInt())
        } catch (e: NumberFormatException) {
            return ScannerResponse.Error.NotFound
        }
        return when (result) {
            DCSSDK_RESULT.DCSSDK_RESULT_SUCCESS -> {
                mutableScannerEvents.tryEmit(ScannerEvent.External.NotConnected)
                ScannerResponse.Success.Disconnect
            }

            DCSSDK_RESULT.DCSSDK_RESULT_FAILURE -> ScannerResponse.Error.Disconnect

            else -> ScannerResponse.Error.NotFound
        }
    }

    override fun dcssdkEventCommunicationSessionEstablished(scannerInfo: DCSScannerInfo?) {
        if (scannerInfo == null) return
        availableScannersFlow.value = fetchScanners()
    }

    override fun dcssdkEventCommunicationSessionTerminated(scannerId: Int) {
        availableScannersFlow.value = fetchScanners()
    }

    override fun dcssdkEventBarcode(barcodeData: ByteArray?, barcodeType: Int, scannerId: Int) {
        barcodeData?.let {
            mutableScannerEvents.tryEmit(ScannerEvent.ScanResult(String(it, Charsets.ISO_8859_1), scanKey))
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

    override fun onLastConnectedScannerDetect(device: BluetoothDevice?): Boolean = false
    override fun onConnectingToLastConnectedScanner(device: BluetoothDevice) = Unit
    override fun onScannerDisconnect() = Unit

    private fun fetchScanners(): List<Scanner> {
        val btEnabled = bluetoothManager.adapter.isEnabled
        if (!hasPermissions() || !btEnabled) {
            return emptyList()
        }

        return (sdkHandler.dcssdkGetAvailableScannersList() + sdkHandler.dcssdkGetActiveScannersList())
            .distinctBy { it.scannerID }
            .map { scannerInfo ->
                Scanner(
                    id = scannerInfo.scannerID.toString(),
                    name = scannerInfo.scannerName,
                    serialNumber = scannerInfo.scannerHWSerialNumber,
                    isConnected = scannerInfo.isActive,
                )
            }
    }

    private fun createIntentFilter(): IntentFilter = IntentFilter().apply {
        addAction(ACTION_BOND_STATE_CHANGED)
    }

    private inner class ZebraBarcodeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == ACTION_BOND_STATE_CHANGED) {
                availableScannersFlow.value = fetchScanners()
            }
        }
    }

    private fun hasPermissions(): Boolean {
        return BLUETOOTH_PERMISSIONS
            .map { context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
            .all { it }
    }

    companion object {
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
