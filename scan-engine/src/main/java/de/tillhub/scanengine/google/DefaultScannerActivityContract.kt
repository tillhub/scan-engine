package de.tillhub.scanengine.google

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.google.ui.GoogleScanningActivity

internal class DefaultScannerActivityContract : ActivityResultContract<Unit, ScannerEvent>() {

    override fun createIntent(context: Context, input: Unit) =
        Intent(context, GoogleScanningActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): ScannerEvent =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.let {
            ScannerEvent.Success(
                value = it.getStringExtra(GoogleScanningActivity.DATA_KEY).orEmpty()
            )
        } ?: ScannerEvent.Camera.Canceled
}
