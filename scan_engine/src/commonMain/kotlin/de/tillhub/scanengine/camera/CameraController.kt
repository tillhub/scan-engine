package de.tillhub.scanengine.camera

/**
 * A controller for managing the camera.
 *
 * This class provides methods to start and stop the camera session.
 * It is an expect class, meaning that the actual implementation will be provided
 * by the platform-specific modules (e.g., Android, iOS).
 */
expect class CameraController {
    /**
     * Starts the camera session.
     */
    fun startSession()

    /**
     * Stops the camera session.
     */
    fun stopSession()
}
