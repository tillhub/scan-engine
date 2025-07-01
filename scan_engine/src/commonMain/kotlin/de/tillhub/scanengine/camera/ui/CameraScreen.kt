package de.tillhub.scanengine.camera.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.tillhub.scanengine.camera.PermissionHandler
import de.tillhub.scanengine.camera.getPermissionHandler
import de.tillhub.scanengine.ui.components.BottomButton
import de.tillhub.scanengine.ui.components.Toolbar
import de.tillhub.scanengine.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import de.tillhub.scanengine.resources.Res
import de.tillhub.scanengine.resources.camera_error
import de.tillhub.scanengine.resources.camera_title
import de.tillhub.scanengine.resources.permission_camera_request
import de.tillhub.scanengine.resources.permission_required_message
import de.tillhub.scanengine.resources.permission_required_title

@Preview
@Composable
fun CameraScreen(
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val permissions: PermissionHandler = getPermissionHandler()

    val hasPermission = remember { mutableStateOf(permissions.hasCameraPermission()) }
    val askForPermission = remember { mutableStateOf(false) }
    val cameraError = remember { mutableStateOf(false) }

    AppTheme {
        Scaffold(
            modifier = Modifier,
            topBar = {
                Toolbar(
                    title = if (hasPermission.value) {
                        stringResource(Res.string.camera_title)
                    } else {
                        stringResource(Res.string.permission_required_title)
                    },
                    onClick = { onDismiss() }
                )
            }
        ) { innerPadding ->
            when {
                cameraError.value -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(top = 16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(
                                vertical = 16.dp,
                                horizontal = 8.dp
                            ),
                            text = stringResource(Res.string.camera_error)
                        )
                    }
                }
                hasPermission.value -> cameraPreview(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(top = 16.dp),
                    barcodeScanned = onResult,
                    onCameraError = { error ->
                        cameraError.value = true
                    }
                )
                askForPermission.value -> permissions.RequestCameraPermission(
                    onGranted = { hasPermission.value = true },
                    onDenied = { askForPermission.value = false }
                )
                else -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(
                                vertical = 16.dp,
                                horizontal = 16.dp
                            ),
                            text = stringResource(Res.string.permission_required_message)
                        )
                        BottomButton(
                            text = stringResource(Res.string.permission_camera_request),
                            onClick = { askForPermission.value = true }
                        )
                    }
                }
            }
        }
    }
}

@Composable
expect fun cameraPreview(
    modifier: Modifier,
    barcodeScanned: (String) -> Unit,
    onCameraError: (String) -> Unit
)
