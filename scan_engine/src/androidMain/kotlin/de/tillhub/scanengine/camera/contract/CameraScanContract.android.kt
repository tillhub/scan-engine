package de.tillhub.scanengine.camera.contract

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import de.tillhub.scanengine.camera.ui.CameraScanActivity
import de.tillhub.scanengine.data.ScannerEvent

@Composable
actual fun rememberCameraScanLauncher(onResult: (ScannerEvent) -> Unit): CameraScanContract {
    val context = LocalContext.current
    val launchCallback = remember { mutableStateOf<((String?) -> Unit)?>(null) }

    var scanKey: String? = null

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val resultData = result.data?.extras?.getString(CameraScanActivity.DATA_KEY)

        val scanResult = if (result.resultCode == Activity.RESULT_OK && !resultData.isNullOrEmpty()) {
            ScannerEvent.ScanResult(
                value = resultData,
                scanKey = scanKey
            )
        } else {
            ScannerEvent.Camera.Canceled
        }

        onResult(scanResult)
    }

    LaunchedEffect(Unit) {
        launchCallback.value = {
            scanKey = it
            val intent = Intent(context, CameraScanActivity::class.java)
            launcher.launch(intent)
        }
    }

    return remember {
        object : CameraScanContract {
            override fun launchCameraScanner(scanKey: String?) {
                launchCallback.value?.invoke(scanKey)
            }
        }
    }
}