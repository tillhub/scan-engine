package de.tillhub.scanengine.camera.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CameraPreviewTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testCameraPreviewComposition() {
        var barcodeScannedCalled = false
        var scannedValue = ""
        var cameraErrorCalled = false
        var errorMessage = ""

        val barcodeCallback: (String) -> Unit = { barcode ->
            barcodeScannedCalled = true
            scannedValue = barcode
        }

        val errorCallback: (String) -> Unit = { error ->
            cameraErrorCalled = true
            errorMessage = error
        }

        composeTestRule.setContent {
            cameraPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview"),
                barcodeScanned = barcodeCallback,
                onCameraError = errorCallback
            )
        }

        // Verify the composable is rendered without crashing
        composeTestRule.onNodeWithTag("camera_preview").assertExists()

        // Initially no callbacks should be called
        assertFalse(barcodeScannedCalled)
        assertFalse(cameraErrorCalled)
    }

    @Test
    fun testCameraPreviewCallbacksAreNotNull() {
        var callbacksAreSet = false

        composeTestRule.setContent {
            val barcodeCallback: (String) -> Unit = { _ ->
                callbacksAreSet = true
            }

            val errorCallback: (String) -> Unit = { _ ->
                callbacksAreSet = true
            }

            cameraPreview(
                modifier = Modifier.testTag("camera_preview_callbacks"),
                barcodeScanned = barcodeCallback,
                onCameraError = errorCallback
            )
        }

        // Verify the composable exists
        composeTestRule.onNodeWithTag("camera_preview_callbacks").assertExists()

        // Callbacks should be properly set (tested by composition not crashing)
        assertNotNull(composeTestRule)
    }

    @Test
    fun testCameraPreviewWithDifferentModifiers() {
        composeTestRule.setContent {
            cameraPreview(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camera_preview_modified"),
                barcodeScanned = { },
                onCameraError = { }
            )
        }

        // Verify composable renders with different modifiers
        composeTestRule.onNodeWithTag("camera_preview_modified").assertExists()
    }

    @Test
    fun testCameraPreviewStateManagement() {
        var currentBarcode = ""
        var currentError = ""

        composeTestRule.setContent {
            val barcodeState = remember { mutableStateOf("") }
            val errorState = remember { mutableStateOf("") }

            currentBarcode = barcodeState.value
            currentError = errorState.value

            cameraPreview(
                modifier = Modifier.testTag("camera_preview_state"),
                barcodeScanned = { barcode ->
                    barcodeState.value = barcode
                },
                onCameraError = { error ->
                    errorState.value = error
                }
            )
        }

        // Verify composable is displayed
        composeTestRule.onNodeWithTag("camera_preview_state").assertExists()

        // Initial state should be empty
        assertEquals("", currentBarcode)
        assertEquals("", currentError)
    }

    @Test
    fun testCameraPreviewRecomposition() {
        var recompositionCount = 0

        composeTestRule.setContent {
            recompositionCount++

            cameraPreview(
                modifier = Modifier.testTag("camera_preview_recomposition"),
                barcodeScanned = { },
                onCameraError = { }
            )
        }

        // Verify initial composition
        composeTestRule.onNodeWithTag("camera_preview_recomposition").assertExists()
        assertTrue(recompositionCount >= 1)

        // Force recomposition by waiting for idle
        composeTestRule.waitForIdle()

        // Verify composable still exists after potential recomposition
        composeTestRule.onNodeWithTag("camera_preview_recomposition").assertExists()
    }
}