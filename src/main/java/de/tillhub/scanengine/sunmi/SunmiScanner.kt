package de.tillhub.scanengine.sunmi

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScanEventProvider
import de.tillhub.scanengine.ScannedData
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.ScannerConnection
import de.tillhub.scanengine.common.CoroutineScopeProvider
import de.tillhub.scanengine.common.CoroutineScopeProviderImpl
import de.tillhub.scanengine.common.safeLet
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@Singleton
class SunmiScanner @Inject constructor() : Scanner {

    private val coroutineScopeProvider: CoroutineScopeProvider = CoroutineScopeProviderImpl()

    private val scanEventProvider = ScanEventProvider()

    private var activeScannerConnection: SunmiScannerConnection? = null

    private var nextScanKey: String? = null

    private val mutablePausedFlow = MutableStateFlow(false)

    override fun connect(activity: Activity): ScannerConnection {
        // TODO [ECR-102] check if sunmi scanner is supported (present) and return null in this case.
        //  Also add a general mechanism to detect if a scanner is available or not
        return SunmiScannerConnection(
            BarcodeScannerConnection(activity, scanEventProvider),
            createCameraScannerConnection(activity)
        ).apply {
            activeScannerConnection = this

            coroutineScopeProvider.applicationScope.launch {
                activeScannerConnection?.cameraScannerConnection?.scannerOpenFlow?.collect {
                    mutablePausedFlow.value = it
                }
            }
        }
    }

    private fun createCameraScannerConnection(activity: Activity): CameraScannerConnection? = when (activity) {
        is ComponentActivity -> CameraScannerConnection(activity, scanEventProvider)
        else -> {
            Timber.w(
                "Scanner could not be connected: Wrong activity type (ComponentActivity needed) - was: %s",
                activity.javaClass
            )
            null
        }
    }

    override fun disconnect(connection: ScannerConnection) {
        if (activeScannerConnection == connection) {
            activeScannerConnection = null
        }
        connection.disconnect()
    }

    override fun scanCameraCode(scanMultipleCodes: Boolean, scanKey: String?) {
        activeScannerConnection?.cameraScannerConnection?.scanCameraCode(scanKey ?: nextScanKey)
    }

    override fun scanNextWithKey(scanKey: String) {
        nextScanKey = scanKey
        activeScannerConnection?.barcodeScannerConnection?.scanNextWithKey(scanKey)
    }

    override fun clearScanKey() {
        nextScanKey = null
        activeScannerConnection?.barcodeScannerConnection?.clearScanKey()
    }

    override fun scanResults(): Flow<ScanEvent> = scanEventProvider.scanEvents

    override fun scanningInProgress(): Flow<Boolean> = mutablePausedFlow

    override fun postScannedData(scannedData: ScannedData) = scanEventProvider.addScanResult(scannedData)
}

private class SunmiScannerConnection(
    val barcodeScannerConnection: BarcodeScannerConnection?,
    val cameraScannerConnection: CameraScannerConnection?,
) : ScannerConnection() {

    override fun disconnect() {
        barcodeScannerConnection?.disconnect()
        cameraScannerConnection?.disconnect()
    }
}

private class BarcodeScannerConnection(
    private val activity: Activity,
    private val scanEventProvider: ScanEventProvider,
) {

    private var broadcastReceiver: SunmiBarcodeScannerBroadcastReceiver? = null

    init {
        connect()
    }

    private fun connect() {
        broadcastReceiver = SunmiBarcodeScannerBroadcastReceiver(scanEventProvider)
        activity.registerReceiver(broadcastReceiver, createIntentFilter())
    }

    fun disconnect() {
        broadcastReceiver?.let { activity.unregisterReceiver(broadcastReceiver) }
        broadcastReceiver = null
    }

    companion object {
        private const val ACTION_DATA_CODE_RECEIVED = "com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED"

        private fun createIntentFilter(): IntentFilter = IntentFilter().apply {
            addAction(ACTION_DATA_CODE_RECEIVED)
        }
    }

    fun scanNextWithKey(scanKey: String) {
        broadcastReceiver?.nextScanKey = scanKey
    }

    fun clearScanKey() {
        broadcastReceiver?.nextScanKey = null
    }

    private class SunmiBarcodeScannerBroadcastReceiver(private val scanEventProvider: ScanEventProvider) :
        BroadcastReceiver() {
        var nextScanKey: String? = null

        override fun onReceive(context: Context, intent: Intent) {
            val code = intent.getStringExtra(DATA)
            if (!code.isNullOrEmpty()) {
                Timber.v("scanned code: %s", code)
                scanEventProvider.addScanResult(ScannedData(code, nextScanKey))
            }
        }

        companion object {
            private const val DATA = "data"
        }
    }
}

class CameraScannerConnection(
    activity: ComponentActivity,
    private val scanEventProvider: ScanEventProvider,
) {
    private val mutableScannerOpenFlow = MutableStateFlow(false)
    val scannerOpenFlow: Flow<Boolean> = mutableScannerOpenFlow

    private var scanKey: String? = null

    private var cameraScannerResult: ActivityResultLauncher<Intent>? =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            mutableScannerOpenFlow.value = false
            result.data?.extras?.let {
                evaluateScanResult(it)
            }
        }

    @Suppress("TooGenericExceptionCaught")
    internal fun scanCameraCode(scanKey: String?) {
        this.scanKey = scanKey
        try {
            mutableScannerOpenFlow.value = true
            cameraScannerResult?.launch(scanIntent())
        } catch (e: Exception) {
            Timber.w(e, "camera scanner could not be started.")
        }
    }

    internal fun disconnect() {
        cameraScannerResult = null
    }

    private fun evaluateScanResult(extras: Bundle) {
        try {
            @Suppress("UNCHECKED_CAST")
            val rawCodes = extras.getSerializable("data") as List<Map<String, String>>
            val result = parseScanResults(rawCodes)

            if (result.isNotEmpty()) {
                Timber.i("scan codes returned %s", result)
                result.forEach {
                    scanEventProvider.addScanResult(ScannedData(it.content, scanKey))
                }
                scanKey = null
            }
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // nothing to do
        }
    }

    private fun parseScanResults(rawCodes: List<Map<String, String>>): List<ScanCode> {
        return rawCodes.mapNotNull {
            safeLet(it[RESPONSE_TYPE], it[RESPONSE_VALUE]) { type, value -> ScanCode(type.toScanCodeType(), value) }
        }
    }

    companion object {
        const val RESPONSE_TYPE = "TYPE"
        const val RESPONSE_VALUE = "VALUE"

        private fun scanIntent(scanMultipleCodes: Boolean = false): Intent =
            Intent("com.summi.scan").apply {
                setPackage("com.sunmi.sunmiqrcodescanner")

                // Additional intent options:
                //
                // The current preview resolution ,PPI_1920_1080 = 0X0001;PPI_1280_720 = 0X0002;PPI_BEST = 0X0003;
                // putExtra("CURRENT_PPI", 0X0003)
                //
                // Whether to identify inverse code
                // putExtra("IDENTIFY_INVERSE_QR_CODE", true)
                //
                // Vibrate after scanning, default false, only support M1 right now.
                // putExtra("PLAY_VIBRATE", false)

                // Prompt tone after scanning (default true)
                putExtra("PLAY_SOUND", true)

                // Whether to display the settings button at the top-right corner (default true)
                putExtra("IS_SHOW_SETTING", true)

                // Whether to display the album button (default true)
                putExtra("IS_SHOW_ALBUM", false)

                // Whether to identify several codes at once (default false)
                putExtra("IDENTIFY_MORE_CODE", scanMultipleCodes)
            }
    }

    private data class ScanCode(
        val codeType: ScanCodeType,
        val content: String,
    )

    sealed class ScanCodeType {
        data class Type(val code: String) : ScanCodeType()
        object Unknown : ScanCodeType()
    }

    private fun String?.toScanCodeType() = when (this) {
        null -> ScanCodeType.Unknown
        else -> ScanCodeType.Type(this)
    }
}
