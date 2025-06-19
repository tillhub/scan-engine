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
import de.tillhub.scanengine.ui.components.getModifierBasedOnDeviceType
import de.tillhub.scanengine.ui.theme.TabletScaffoldModifier
import de.tillhub.scanengine.ui.theme.AppTheme
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import de.tillhub.scanengine.resources.Res
import de.tillhub.scanengine.resources.camera_title
import de.tillhub.scanengine.resources.permission_camera_request

@Preview
@Composable
fun CameraScreen(
    onResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val permissions: PermissionHandler = getPermissionHandler()

    val hasPermission = remember { mutableStateOf(permissions.hasCameraPermission()) }
    val askForPermission = remember { mutableStateOf(false) }

    AppTheme {
        Scaffold(
            modifier = getModifierBasedOnDeviceType(
                isTablet = TabletScaffoldModifier,
                isMobile = Modifier
            ),
            topBar = {
                Toolbar(
                    title = if (hasPermission.value) {
                        stringResource(Res.string.camera_title)
                    } else {
                        stringResource(Res.string.camera_title)
                    },
                    onClick = { onDismiss() }
                )
            }
        ) { innerPadding ->
            when {
                hasPermission.value -> cameraPreview(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(top = 16.dp),
                    barcodeScanned = onResult
                )
                askForPermission.value -> permissions.RequestCameraPermission(
                    onGranted = { hasPermission.value = true },
                    onDenied = { askForPermission.value = false }
                )
                else -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(top = 16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier.padding(vertical = 16.dp),
                            text = stringResource(Res.string.permission_camera_request)
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
    barcodeScanned: (String) -> Unit
)
