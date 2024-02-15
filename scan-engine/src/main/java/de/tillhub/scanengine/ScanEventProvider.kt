package de.tillhub.scanengine

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * Utility class for converting single scan results into a stream of data which can be observed everywhere.
 */
class ScanEventProvider {

    private val mutableScanEvents = MutableSharedFlow<ScanEvent>(extraBufferCapacity = 1)
    val scanEvents: SharedFlow<ScanEvent> = mutableScanEvents

    fun addScanResult(content: ScanEvent) {
        mutableScanEvents.tryEmit(content)
    }
}
