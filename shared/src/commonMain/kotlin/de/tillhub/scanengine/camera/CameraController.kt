package de.tillhub.scanengine.camera

expect class CameraController {
    /**
     * Starts the camera session.
     */
    fun startSession()

    /**
     * Stops the camera session.
     */
    fun stopSession()

    fun setScanKey(scanKey: String?)
}