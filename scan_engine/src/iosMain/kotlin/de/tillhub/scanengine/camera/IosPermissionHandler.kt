package de.tillhub.scanengine.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

class IosPermissionHandler : PermissionHandler {
    override fun hasCameraPermission(): Boolean {
        val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
        return status == AVAuthorizationStatusAuthorized
    }

    @Composable
    override fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit) {
        AVCaptureDevice.requestAccessForMediaType(
            AVMediaTypeVideo
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
actual fun getPermissionHandler(): PermissionHandler {
    return remember {
        IosPermissionHandler()
    }
}