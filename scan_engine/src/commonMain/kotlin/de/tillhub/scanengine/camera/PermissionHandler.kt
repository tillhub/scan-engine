package de.tillhub.scanengine.camera

import androidx.compose.runtime.Composable

/**
 * Interface for handling camera permission requests.
 *
 * This interface provides methods to check and request camera permission,
 * crucial for features that require access to the device's camera.
 */
internal interface PermissionHandler {
    /**
     * Checks if the camera permission is granted.
     *
     * @return True if granted, false otherwise.
     */
    fun hasCameraPermission(): Boolean

    /**
     * Requests camera permission.
     *
     * @param onGranted Callback invoked when permission is granted.
     * @param onDenied Callback invoked when permission is denied.
     */
    @Composable
    fun requestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit)
}

@Composable
internal expect fun getPermissionHandler(): PermissionHandler
