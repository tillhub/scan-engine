package de.tillhub.scanengine

import android.app.Activity
import kotlinx.coroutines.flow.Flow

/**
 * Used for connecting and disconnecting a scanner, issue scan commands and observing scanned codes.
 */
interface Scanner {

    /**
     * Connects the scanner to the given [Activity]. When the connection was successful a [ScannerConnection]
     * is returned. This connection must be used to disconnect when the activity is destroyed using [disconnect].
     * When a [ScannerConnection] is obtained it will be used internally to make any further processing of
     * the scanner hardware. Any additional calls to [connect] will result in a new [ScannerConnection] which
     * is then used instead of the old one. Caution: the old [ScannerConnection] will not be disconnected
     * automatically. This needs to be implemented by the caller.
     *
     * The scanner results can be obtained via [scanResults].
     */
    fun connect(activity: Activity): ScannerConnection?

    /**
     * Disconnects the [ScannerConnection]. If another [ScannerConnection] is currently being used
     * internally (i.e. another activity was started before the old one was destroyed) it will not be affected and
     * work as intended. However the passed [ScannerConnection] will be disconnected in any case to prevent
     * memory leaks.
     */
    fun disconnect(connection: ScannerConnection)

    /**
     * Can be used to observe any scanned code.
     */
    fun scanResults(): Flow<ScanEvent>

    /**
     * Can be used to observe if scanning is in progress.
     */
    fun scanningInProgress(): Flow<Boolean>

    /**
     * Start the camera based scanner.
     */
    fun scanCameraCode(scanMultipleCodes: Boolean = false, scanKey: String? = null)

    /**
     * Set the scanKey that will be used with next scanning event
     */
    fun scanNextWithKey(scanKey: String)

    /**
     * Clear the scanKey that is set to be used with next scanning event
     */
    fun clearScanKey()

    /**
     * Can be used to post custom scan events in code.
     */
    fun postScannedData(scannedData: ScannedData)
}

/**
 * Represents a connection to a scanner. When the scanner is not used anymore it must be disconnected using
 * [Scanner.disconnect].
 */
@Suppress("UnnecessaryAbstractClass")
abstract class ScannerConnection {
    internal abstract fun disconnect()
}
