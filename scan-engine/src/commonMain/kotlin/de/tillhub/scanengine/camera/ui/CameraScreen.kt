package de.tillhub.scanengine.camera.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import de.tillhub.scanengine.camera.PermissionHandler
import de.tillhub.scanengine.camera.getPermissionHandler
import de.tillhub.scanengine.resources.Res
import de.tillhub.scanengine.resources.camera_access_required
import de.tillhub.scanengine.resources.camera_error
import de.tillhub.scanengine.resources.camera_title
import de.tillhub.scanengine.resources.ic_camera
import de.tillhub.scanengine.resources.permission_camera_request
import de.tillhub.scanengine.resources.permission_required_message
import de.tillhub.scanengine.resources.permission_required_title
import de.tillhub.scanengine.ui.components.BottomButton
import de.tillhub.scanengine.ui.components.Toolbar
import de.tillhub.scanengine.ui.theme.AppTheme
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/**
 * A Composable function that displays a camera screen for barcode scanning.
 *
 * This screen handles camera permission requests and displays appropriate UI based on the permission status.
 * If permission is granted, it shows the camera preview for scanning.
 * If permission is denied, it shows a message requesting permission.
 * If there's a camera error, it displays an error message.
 *
 * @param permissions An instance of [PermissionHandler] used to manage camera permissions.
 *                    Defaults to a platform-specific implementation obtained via [getPermissionHandler].
 * @param onResult A callback function that is invoked when a barcode is successfully scanned.
 *                 It receives the scanned barcode string as a parameter.
 * @param onDismiss A callback function that is invoked when the user dismisses the screen
 *                  (e.g., by clicking the back button in the toolbar).
 */
@Composable
internal fun CameraScreen(
    permissions: PermissionHandler = getPermissionHandler(),
    onResult: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val hasPermission = remember { mutableStateOf(permissions.hasCameraPermission()) }
    val askForPermission = remember { mutableStateOf(false) }
    val cameraError = remember { mutableStateOf(false) }

    AppTheme {
        Scaffold(
            modifier = Modifier,
            containerColor = Color.White,
            topBar = {
                val title = if (!hasPermission.value && !cameraError.value) {
                    stringResource(Res.string.permission_required_title)
                } else {
                    stringResource(Res.string.camera_title)
                }
                Toolbar(
                    title = title,
                    onClick = { onDismiss() },
                )
            },
        ) { innerPadding ->
            when {
                cameraError.value -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(top = 16.dp)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier
                                .padding(
                                    vertical = 16.dp,
                                    horizontal = 8.dp,
                                )
                                .semantics { contentDescription = "Camera Error" },
                            text = stringResource(Res.string.camera_error),
                        )
                    }
                }
                hasPermission.value -> cameraPreview(
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(top = 16.dp)
                        .semantics { contentDescription = "Camera preview" },
                    barcodeScanned = onResult,
                    onCameraError = { _ ->
                        cameraError.value = true
                    },
                )
                askForPermission.value -> permissions.requestCameraPermission(
                    onGranted = { hasPermission.value = true },
                    onDenied = { askForPermission.value = false },
                )
                else -> {
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Image(
                                painter = painterResource(Res.drawable.ic_camera),
                                contentDescription = "Camera icon",
                                modifier = Modifier.size(48.dp),
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(Res.string.camera_access_required),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier
                                    .semantics {
                                        contentDescription = "Permission title"
                                    },
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.permission_required_message),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .semantics {
                                        contentDescription = "Permission explanation"
                                    },
                            )
                        }
                        BottomButton(
                            modifier = Modifier
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .testTag("submitButton"),
                            text = stringResource(Res.string.permission_camera_request),
                            onClick = { askForPermission.value = true },
                        )
                    }
                }
            }
        }
    }
}

/**
 * A Composable function that displays the camera preview for barcode scanning.
 * This is an expect function, meaning its actual implementation is provided by the platform-specific code (Android or iOS).
 *
 * @param modifier Modifier to be applied to the camera preview.
 * @param barcodeScanned A callback function that is invoked when a barcode is successfully scanned.
 *                       It receives the scanned barcode string as a parameter.
 * @param onCameraError A callback function that is invoked when an error occurs with the camera.
 *                      It receives an error message string as a parameter.
 */
@Composable
internal expect fun cameraPreview(
    modifier: Modifier,
    barcodeScanned: (String) -> Unit,
    onCameraError: (String) -> Unit,
)
