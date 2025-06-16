package de.tillhub.scanengine

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tillhub.scanengine.camera.ui.CameraScreen
import de.tillhub.scanengine.common.SingletonHolder
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

class ScanEngine private constructor() {

    private val mutableScannerEvents = MutableStateFlow<ScannerEvent>(ScannerEvent.External.NotConnected)

    fun observeScannerResults(): Flow<ScannerEvent> = mutableScannerEvents.drop(1)

    @Composable
    fun getCameraView(scanKey: String?, modifier: Modifier = Modifier) {
        CameraScreen(
            modifier = modifier,
            scanKey = scanKey,
            scannerEvents = mutableScannerEvents
        )
    }

    companion object : SingletonHolder<ScanEngine>(::ScanEngine)
}
