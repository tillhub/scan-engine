package de.tillhub.scanengine.camera

import androidx.compose.runtime.Composable

interface PermissionHandler {
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
    fun RequestCameraPermission(onGranted: () -> Unit, onDenied: () -> Unit)
}

@Composable
expect fun getPermissionHandler(): PermissionHandler