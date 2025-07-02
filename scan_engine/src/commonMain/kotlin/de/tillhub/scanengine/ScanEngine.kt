package de.tillhub.scanengine

import de.tillhub.scanengine.camera.DefaultCameraScanner
import de.tillhub.scanengine.common.SingletonHolder
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.drop

/**
 * A central hub for managing and observing scanning events from various sources.
 *
 * This class acts as a singleton and provides methods to:
 * - Observe scanner results as a [Flow] of [ScannerEvent].
 * - Create new instances of [CameraScanner] for camera-based scanning.
 *
 * The `ScanEngine` manages a [MutableStateFlow] of [ScannerEvent] internally,
 * which is updated by the different scanner implementations.
 *
 * Example usage:
 * ```kotlin
 * // Obtain the singleton instance
 * val scanEngine = ScanEngine.getInstance()
 *
 * // Observe scanner results
 * scanEngine.observeScannerResults().collect { event ->
 *     when (event) {
 *         is ScannerEvent.Success -> {
 *             // Handle successful scan
 *             println("Scanned code: ${event.scanCode.value}")
 *         }
 *         is ScannerEvent.Error -> {
 *             // Handle error
 *             println("Scan error: ${event.exception.message}")
 *         }
 *         // Handle other event types
 *         ...
 *     }
 * }
 *
 * // Create a new camera scanner
 * val cameraScanner = scanEngine.newCameraScanner()
 * // Use the cameraScanner instance
 * ```
 */
class ScanEngine private constructor() {

    private val mutableScannerEvents = MutableStateFlow<ScannerEvent>(ScannerEvent.External.NotConnected)

    /**
     * Provides a [Flow] of [ScannerEvent] that emits scanner results.
     *
     * The first event (initial value) is skipped using `drop(1)` because it's a default
     * `ScannerEvent.External.NotConnected` state and not a real scan event.
     * Subscribers will receive subsequent events as they are emitted by the connected scanners.
     *
     * @return A [Flow] of [ScannerEvent] representing scanner results.
     */
    fun observeScannerResults(): Flow<ScannerEvent> = mutableScannerEvents.drop(1)

    /**
     * Creates and returns a new instance of [CameraScanner].
     *
     * This method provides a way to obtain a dedicated camera scanner instance
     * that will report its scan events to this `ScanEngine`.
     *
     * @return A new [CameraScanner] instance.
     */
    fun newCameraScanner(): CameraScanner {
        return DefaultCameraScanner(mutableScannerEvents)
    }

    companion object : SingletonHolder<ScanEngine>(::ScanEngine)
}
