package de.tillhub.scanengine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * Utility class for converting single scan results into a stream of data which can be observed everywhere.
 */
class ScanEventProvider {

    private val mutableScanEvents = MutableSharedFlow<ScanEvent>(extraBufferCapacity = 1)
    val scanEvents: Flow<ScanEvent> = mutableScanEvents

    fun addScanResult(content: ScannedDataResult) {
        mutableScanEvents.tryEmit(ScanEvent.Success(content))
    }
}
