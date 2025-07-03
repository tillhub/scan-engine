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

/**
 * A Composable function that remembers a [CameraScanContract] for launching a camera scanner.
 *
 * This function handles the lifecycle of the camera scanner and provides a way to launch it
 * and receive the results. It uses [rememberLauncherForActivityResult] to manage the
 * activity result contract.
 *
 * @param onResult A callback function that will be invoked with the [ScannerEvent]
 *                 when the camera scanning operation is completed or canceled.
 *                 - [ScannerEvent.ScanResult]: Contains the scanned data if successful.
 *                 - [ScannerEvent.Camera.Canceled]: Indicates the camera scanning was canceled by the user.
 * @return A [CameraScanContract] instance that can be used to launch the camera scanner.
 *         Call [CameraScanContract.launchCameraScanner] on this instance to start the scanning process.
 *
 * @see CameraScanContract
 * @see CameraScanActivity
 * @see ScannerEvent
 */
@Composable
internal actual fun rememberCameraScanLauncher(onResult: (ScannerEvent) -> Unit): CameraScanContract {
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
                onResult.invoke(ScannerEvent.Camera.InProgress(scanKey))

                launchCallback.value?.invoke(scanKey)
            }
        }
    }
}