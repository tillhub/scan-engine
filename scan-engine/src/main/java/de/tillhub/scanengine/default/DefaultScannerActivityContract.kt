package de.tillhub.scanengine.default

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.default.ui.GoogleScanningActivity

internal class DefaultScannerActivityContract : ActivityResultContract<Unit, ScanEvent>() {

    override fun createIntent(context: Context, input: Unit) =
        Intent(context, GoogleScanningActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): ScanEvent =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.let {
            ScanEvent.Success(
                value = it.getStringExtra(GoogleScanningActivity.DATA_KEY).orEmpty()
            )
        } ?: ScanEvent.Canceled
}
