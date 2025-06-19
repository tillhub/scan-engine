package de.tillhub.scanengine.camera.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
actual fun cameraPreview(
    modifier: Modifier,
    barcodeScanned: (String) -> Unit
) {

}