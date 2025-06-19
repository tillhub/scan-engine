package de.tillhub.scanengine

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import de.tillhub.scanengine.camera.DefaultCameraScanner
import de.tillhub.scanengine.camera.ui.CameraScreen
import de.tillhub.scanengine.common.SingletonHolder
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.ScannerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

class ScanEngine private constructor() {

    private val mutableScannerEvents = MutableStateFlow<ScannerEvent>(ScannerEvent.External.NotConnected)

    fun observeScannerResults(): Flow<ScannerEvent> = mutableScannerEvents.drop(1)

    fun newCameraScanner(): CameraScanner {
        return DefaultCameraScanner(mutableScannerEvents)
    }

    companion object : SingletonHolder<ScanEngine>(::ScanEngine)
}
