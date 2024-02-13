import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.default.ui.GoogleScanningActivity

class DefaultScannerActivityContract : ActivityResultContract<String, ScanEvent>() {

    override fun createIntent(context: Context, input: String) =
        Intent(context, GoogleScanningActivity::class.java)
    override fun parseResult(resultCode: Int, intent: Intent?): ScanEvent =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.let {
            ScanEvent.Success(value = it.getStringExtra(GoogleScanningActivity.DATA_KEY).orEmpty())
        } ?: ScanEvent.Canceled
}
