package de.tillhub.scanengine.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScanEventProvider
import de.tillhub.scanengine.ScannedData
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.ScannerConnection
import de.tillhub.scanengine.google.ui.GoogleScanningActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class GoogleScanner constructor(
    private val appContext: Context
) : Scanner {
    @VisibleForTesting
    val scanEventProvider = ScanEventProvider()
    @VisibleForTesting
    var activeScannerConnection: GoogleScannerConnection? = null

    private var nextScanKey: String? = null

    private val mutablePausedFlow = MutableStateFlow(false)

    override fun connect(activity: Activity): ScannerConnection? =
        when (activity) {
            is ComponentActivity -> GoogleScannerConnection(activity, appContext, scanEventProvider).apply {
                activeScannerConnection = this
            }
            else -> null
        }

    override fun disconnect(connection: ScannerConnection) {
        if (activeScannerConnection == connection) {
            activeScannerConnection = null
        }
        connection.disconnect()
    }

    override fun scanResults(): Flow<ScanEvent> = scanEventProvider.scanEvents

    override fun scanningInProgress(): Flow<Boolean> = mutablePausedFlow

    override fun scanCameraCode(scanMultipleCodes: Boolean, scanKey: String?) {
        activeScannerConnection?.scanCameraCode(scanKey ?: nextScanKey)
    }

    override fun scanNextWithKey(scanKey: String) {
        nextScanKey = scanKey
    }

    override fun clearScanKey() {
        nextScanKey = null
    }

    override fun postScannedData(scannedData: ScannedData) = scanEventProvider.addScanResult(scannedData)
}

class GoogleScannerConnection(
    activity: ComponentActivity,
    private val appContext: Context,
    private val scanEventProvider: ScanEventProvider,
) : ScannerConnection() {

    private var scanKey: String? = null

    private var cameraScannerResult: ActivityResultLauncher<Intent>? =
        activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            result.data?.extras?.let {
                evaluateScanResult(it)
            }
        }

    @Suppress("TooGenericExceptionCaught")
    internal fun scanCameraCode(scanKey: String?) {
        this.scanKey = scanKey
        try {
            cameraScannerResult?.launch(scanIntent(appContext))
        } catch (e: Exception) {
            Timber.w(e, "camera scanner could not be started.")
        }
    }

    override fun disconnect() {
        cameraScannerResult = null
    }

    private fun evaluateScanResult(extras: Bundle) {
        try {
            val result = extras.getString(GoogleScanningActivity.DATA_KEY).orEmpty()

            if (result.isNotEmpty()) {
                scanEventProvider.addScanResult(ScannedData(result, scanKey))
            }
            scanKey = null
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            // nothing to do
        }
    }

    companion object {
        private fun scanIntent(context: Context): Intent =
            Intent(context, GoogleScanningActivity::class.java)
    }
}
