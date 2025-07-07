package de.tillhub.scanengine.camera.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import de.tillhub.scanengine.camera.PermissionHandler
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class CameraScreenTest {

    private var hasPermission = false
    private var permissionGranted: () -> Unit = {}
    private var permissionDenied: () -> Unit = {}

    private val permissionHandler = object : PermissionHandler {
        override fun hasCameraPermission(): Boolean = hasPermission

        @Composable
        override fun requestCameraPermission(
            onGranted: () -> Unit,
            onDenied: () -> Unit
        ) {
            permissionGranted = onGranted
            permissionDenied = onDenied
            Column(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .padding(
                            vertical = 16.dp,
                            horizontal = 16.dp
                        )
                        .semantics { contentDescription = "Permission request" },
                    text = "Permission request"
                )
            }
        }

    }

    @Test
    fun missingPermissions_showPermissionExplanation() = runComposeUiTest {
        setContent {
            CameraScreen(
                permissions = permissionHandler,
                onResult = {},
                onDismiss = {}
            )
        }

        onNodeWithContentDescription("Permission explanation").assertExists()
        onNodeWithTag("submitButton").assertExists()
    }

    @Test
    fun missingPermissions_showPermissionRequest() = runComposeUiTest {
        setContent {
            CameraScreen(
                permissions = permissionHandler,
                onResult = {},
                onDismiss = {}
            )
        }

        onNodeWithTag("submitButton").performClick()

        onNodeWithContentDescription("Permission request").assertExists()
    }

    @Test
    fun missingPermissions_denyPermission() = runComposeUiTest {
        setContent {
            CameraScreen(
                permissions = permissionHandler,
                onResult = {},
                onDismiss = {}
            )
        }

        onNodeWithTag("submitButton").performClick()

        onNodeWithContentDescription("Permission request").assertExists()

        permissionDenied()

        onNodeWithContentDescription("Permission explanation").assertExists()
        onNodeWithTag("submitButton").assertExists()
    }

    @Test
    fun missingPermissions_grantPermission() = runComposeUiTest {
        setContent {
            CameraScreen(
                permissions = permissionHandler,
                onResult = {},
                onDismiss = {}
            )
        }

        onNodeWithTag("submitButton").performClick()

        onNodeWithContentDescription("Permission request").assertExists()

        permissionGranted()

        onNodeWithContentDescription("Camera preview").assertExists()
    }
}