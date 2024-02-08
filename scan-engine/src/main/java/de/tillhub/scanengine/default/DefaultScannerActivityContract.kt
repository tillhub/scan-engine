import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.ScannedDataResult
import de.tillhub.scanengine.Scanner.Companion.SCAN_KEY
import de.tillhub.scanengine.default.ui.GoogleScanningActivity

class DefaultScannerActivityContract : ActivityResultContract<String, ScannedDataResult>() {

    override fun createIntent(context: Context, input: String) =
        Intent(context, GoogleScanningActivity::class.java).apply {
            putExtra(SCAN_KEY, input)
        }
    override fun parseResult(resultCode: Int, intent: Intent?): ScannedDataResult =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.let {
            ScannedDataResult.ScannedData(
                value = it.getStringExtra(GoogleScanningActivity.DATA_KEY).orEmpty(),
                scanKey = it.getStringExtra(SCAN_KEY)
            )
        } ?: ScannedDataResult.Canceled
}
