package de.tillhub.scanengine.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import de.tillhub.scanengine.camera.PermissionHandler
import de.tillhub.scanengine.camera.getPermissionHandler
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun CameraScreen(
    modifier: Modifier = Modifier,
    scanKey: String? = null,
    scannerEvents: MutableStateFlow<ScannerEvent>
) {
    val permissions: PermissionHandler = getPermissionHandler()

    val hasPermission = remember { mutableStateOf(permissions.hasCameraPermission()) }

    if (hasPermission.value) {
        cameraPreview(modifier, scanKey, scannerEvents)
    } else {
        permissions.RequestCameraPermission(
            onGranted = {
                hasPermission.value = true
            },
            onDenied = {
                // TODO
            }
        )
    }
}

@Composable
expect fun cameraPreview(
    modifier: Modifier,
    scanKey: String?,
    scannerEvents: MutableStateFlow<ScannerEvent>
)
