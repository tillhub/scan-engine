package de.tillhub.scanengine.google

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScanEventProvider
import de.tillhub.scanengine.ScannedData
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.google.ui.GoogleScanningActivity
import kotlinx.coroutines.flow.Flow
import java.lang.ref.WeakReference

class DefaultScanner(
    private val activity: WeakReference<ComponentActivity>
) : Scanner, DefaultLifecycleObserver {

    @VisibleForTesting
    val scanEventProvider = ScanEventProvider()

    private var scanKey: String? = null

    private lateinit var cameraScannerResult: ActivityResultLauncher<Intent>

    init {
        activity.get()?.lifecycle?.addObserver(this)
    }

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        cameraScannerResult = activity.get()?.activityResultRegistry?.register(
            CAMERA_SCANNER_KEY,
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            result.data?.extras?.let {
                evaluateScanResult(it)
            }
        } ?: throw IllegalStateException("GoogleScanner: Activity is null")
    }


    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        scanKey = null
        cameraScannerResult.unregister()
        activity.get()?.lifecycle?.removeObserver(this)
        activity.clear()
    }

    override fun observeScannerResults(): Flow<ScanEvent> = scanEventProvider.scanEvents

    override fun startCameraScanner(scanKey: String?) {
        this.scanKey = scanKey
        cameraScannerResult.launch(Intent(activity.get(), GoogleScanningActivity::class.java))
    }

    override fun scanNextWithKey(scanKey: String) {
        this.scanKey = scanKey
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
        const val CAMERA_SCANNER_KEY = "CAMERA_SCANNER_KEY"
    }
}
