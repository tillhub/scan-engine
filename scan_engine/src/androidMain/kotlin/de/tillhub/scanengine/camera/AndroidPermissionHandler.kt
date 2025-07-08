package de.tillhub.scanengine.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat

/**
 * Android-specific implementation of [PermissionHandler] for managing camera permissions.
 *
 * @param context The application context used to check and request permissions.
 */
internal class AndroidPermissionHandler(
    private val context: Context,
) : PermissionHandler {
    /**
     * Checks if the app has been granted camera permission.
     *
     * @return `true` if camera permission is granted, `false` otherwise.
     */
    override fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Composable function to request camera permission.
     *
     * This function checks the current camera permission status.
     * If permission is already granted, the `onGranted` callback is invoked immediately.
     * If permission is denied, it launches a system permission request dialog.
     * The result of the dialog (granted or denied) will trigger the corresponding callback.
     *
     * @param onGranted Callback to be invoked if the camera permission is granted.
     * @param onDenied Callback to be invoked if the camera permission is denied.
     */
    @Composable
    override fun requestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { isGranted ->
                if (isGranted) {
                    onGranted()
                } else {
                    onDenied()
                }
            },
        )

        val permissionStatus = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA,
        )

        when (permissionStatus) {
            PackageManager.PERMISSION_GRANTED -> onGranted()
            PackageManager.PERMISSION_DENIED -> {
                LaunchedEffect(Unit) {
                    launcher.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }
}

/**
 * Composable function that provides an instance of [PermissionHandler].
 *
 * This function is used to obtain a platform-specific implementation of [PermissionHandler].
 * On Android, it returns an [AndroidPermissionHandler] initialized with the current [LocalContext].
 * The `remember` composable ensures that the same [PermissionHandler] instance is reused across recompositions
 * as long as the context remains the same.
 *
 * @return An instance of [PermissionHandler].
 */
@Composable
internal actual fun getPermissionHandler(): PermissionHandler {
    val context = LocalContext.current

    return remember {
        AndroidPermissionHandler(context)
    }
}
