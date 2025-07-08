package de.tillhub.scanengine.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

/**
 * iOS implementation of [PermissionHandler] for managing camera permissions.
 *
 * This class utilizes the `AVFoundation` framework to check and request
 * access to the device's camera.
 */
internal class IosPermissionHandler : PermissionHandler {
    /**
     * Checks if the app has been granted permission to access the camera.
     *
     * This function queries the current authorization status for video media type
     * using `AVCaptureDevice.authorizationStatusForMediaType`.
     *
     * @return `true` if the camera permission is authorized, `false` otherwise.
     */
    override fun hasCameraPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }

    /**
     * Requests permission to access the camera.
     *
     * This function uses `AVCaptureDevice.requestAccessForMediaType` to prompt
     * the user for camera access. The provided callbacks are invoked based on
     * the user's response.
     *
     * This function is composable and should be called from within a Composable context.
     *
     * @param onGranted A lambda function to be executed if the camera permission is granted.
     * @param onDenied A lambda function to be executed if the camera permission is denied.
     */
    @Composable
    override fun requestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        AVCaptureDevice.requestAccessForMediaType(
            AVMediaTypeVideo,
        ) { granted ->
            if (granted) {
                onGranted()
            } else {
                onDenied()
            }
        }
    }
}

@Composable
internal actual fun getPermissionHandler(): PermissionHandler {
    return remember {
        IosPermissionHandler()
    }
}
