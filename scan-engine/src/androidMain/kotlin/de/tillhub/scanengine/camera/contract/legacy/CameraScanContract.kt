package de.tillhub.scanengine.camera.contract.legacy

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.camera.ui.CameraScanActivity
import de.tillhub.scanengine.data.ScannerEvent

class CameraScanContract : ActivityResultContract<String?, ScannerEvent>() {
    override fun createIntent(
        context: Context,
        input: String?,
    ): Intent = Intent(context, CameraScanActivity::class.java).apply {
        input?.let {
            putExtra(CameraScanActivity.SCAN_KEY, it)
        }
    }

    override fun parseResult(
        resultCode: Int,
        intent: Intent?,
    ): ScannerEvent {
        val resultData = intent?.extras?.getString(CameraScanActivity.DATA_KEY)
        val scanKey = intent?.extras?.getString(CameraScanActivity.SCAN_KEY)

        return if (resultCode == Activity.RESULT_OK && !resultData.isNullOrEmpty()) {
            ScannerEvent.ScanResult(
                value = resultData,
                scanKey = scanKey,
            )
        } else {
            ScannerEvent.Camera.Canceled
        }
    }
}
