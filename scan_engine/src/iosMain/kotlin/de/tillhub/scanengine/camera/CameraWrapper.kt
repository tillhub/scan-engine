package de.tillhub.scanengine.camera

import de.tillhub.scanengine.camera.common.dispatchAsync
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.position
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIView
import platform.darwin.NSObject

/**
 * A wrapper class for managing camera operations on iOS devices using AVFoundation.
 *
 * This class encapsulates the setup, control, and preview of the device camera.
 * It provides functionalities to:
 * - Initialize and configure an `AVCaptureSession`.
 * - Start and stop the capture session.
 * - Set up a preview layer to display the camera feed on a `UIView`.
 * - Handle camera-related errors through a customizable `onError` callback.
 *
 * It primarily focuses on using the back-facing wide-angle camera.
 *
 * @property captureSession The active `AVCaptureSession` instance, if initialized.
 * @property cameraPreviewLayer The `AVCaptureVideoPreviewLayer` used to display the camera feed, if set up.
 * @property onError A callback function that is invoked when a [CameraException] occurs.
 */
class CameraWrapper: NSObject() {
    private var currentCamera: AVCaptureDevice? = null
    var captureSession: AVCaptureSession? = null
    var cameraPreviewLayer: AVCaptureVideoPreviewLayer? = null

    var onError: ((CameraException) -> Unit)? = null

    sealed class CameraException : Exception() {
        class DeviceNotAvailable : CameraException()
        class ConfigurationError(message: String) : CameraException()
    }

    /**
     * This function initializes and configures the AVCaptureSession for camera operations.
     * It sets the session preset to AVCaptureSessionPresetPhoto for high-quality still images.
     * It then attempts to set up the camera inputs using the [setupInputs] method.
     * If input setup fails, it throws a [CameraException.DeviceNotAvailable].
     * If any [CameraException] occurs during setup, the session is cleaned up using [cleanupSession],
     * and the [onError] callback is invoked with the exception.
     */    
    internal fun setupSession() {
        try {
            captureSession = AVCaptureSession()
            captureSession?.beginConfiguration()

            captureSession?.sessionPreset = AVCaptureSessionPresetPhoto

            if (!setupInputs()) {
                throw CameraException.DeviceNotAvailable()
            }

            captureSession?.commitConfiguration()
        } catch (e: CameraException) {
            cleanupSession()
            onError?.invoke(e)
        }
    }

    /**
     * Starts the camera capture session if it is not already running.
     * The session is started asynchronously on a separate dispatch queue.
     */
    internal fun startSession() {
        if (captureSession?.isRunning() == false) {
            dispatchAsync {
                captureSession?.startRunning()
            }
        }
    }

    /**
     * Stops the camera capture session if it is currently running.
     */
    internal fun stopSession() {
        if (captureSession?.isRunning() == true) {
            captureSession?.stopRunning()
        }
    }

    /**
     * Sets up the preview layer for displaying the camera feed.
     *
     * This function creates an `AVCaptureVideoPreviewLayer` from the current `captureSession`
     * and adds it as a sublayer to the provided `UIView`. The preview layer is configured
     * to fill the bounds of the view and maintain the aspect ratio of the video.
     * The video orientation is set based on the current device orientation.
     *
     * @param view The `UIView` on which the camera preview will be displayed.
     */
    @OptIn(ExperimentalForeignApi::class)
    internal fun setupPreviewLayer(view: UIView) {
        captureSession?.let { session ->
            val newPreviewLayer = AVCaptureVideoPreviewLayer(session = session).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
                setFrame(view.bounds)
                connection?.videoOrientation = currentVideoOrientation()
            }

            view.layer.addSublayer(newPreviewLayer)
            cameraPreviewLayer = newPreviewLayer
        }
    }

    /**
     * Determines the appropriate `AVCaptureVideoOrientation` based on the current device orientation.
     *
     * This function maps `UIDeviceOrientation` values to their corresponding `AVCaptureVideoOrientation`
     * values. It handles portrait, portrait upside down, landscape left, and landscape right orientations.
     * If the device orientation is unknown or face up/down, it defaults to portrait orientation.
     *
     * Note: `AVCaptureVideoOrientationLandscapeLeft` corresponds to `UIDeviceOrientationLandscapeRight`
     * and vice-versa. This is because `AVCaptureVideoOrientation` refers to the orientation of the
     * video buffer, while `UIDeviceOrientation` refers to the physical orientation of the device.
     *
     * @return The `AVCaptureVideoOrientation` that corresponds to the current device orientation.
     */
    internal fun currentVideoOrientation(): AVCaptureVideoOrientation {
        val orientation = UIDevice.currentDevice.orientation
        return when (orientation) {
            UIDeviceOrientation.UIDeviceOrientationPortrait -> AVCaptureVideoOrientationPortrait
            UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown -> AVCaptureVideoOrientationPortraitUpsideDown
            UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> AVCaptureVideoOrientationLandscapeRight
            UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> AVCaptureVideoOrientationLandscapeLeft
            else -> AVCaptureVideoOrientationPortrait
        }
    }

    /**
     * Cleans up the camera session and related resources.
     *
     * This function stops the capture session, removes the preview layer from its superlayer,
     * and nils out references to the preview layer, capture session, and current camera.
     * This is typically called when the camera is no longer needed or in case of an error.
     */
    private fun cleanupSession() {
        stopSession()
        cameraPreviewLayer?.removeFromSuperlayer()
        cameraPreviewLayer = null
        captureSession = null
        currentCamera = null
    }

    /**
     * Sets up the camera inputs for the `AVCaptureSession`.
     *
     * This function discovers available video capture devices, prioritizing the back-facing
     * wide-angle camera. If a suitable camera is found, it creates an `AVCaptureDeviceInput`
     * from it and attempts to add this input to the `captureSession`.
     *
     * @return `true` if the camera input was successfully set up and added to the session,
     *         `false` otherwise (e.g., if no suitable camera is found or if the input
     *         cannot be added to the session).
     * @throws CameraException.ConfigurationError if an error occurs while creating the
     *         `AVCaptureDeviceInput`.
     */
    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    @OptIn(ExperimentalForeignApi::class)
    private fun setupInputs(): Boolean {
        val availableDevices = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
            AVMediaTypeVideo,
            AVCaptureDevicePositionUnspecified
        ).devices

        (availableDevices.find {
            (it as AVCaptureDevice).position == AVCaptureDevicePositionBack
        } as? AVCaptureDevice)?.let { currentCamera ->
            try {
                val input = AVCaptureDeviceInput.deviceInputWithDevice(
                    currentCamera,
                    null
                )

                if (input != null && captureSession?.canAddInput(input) == true) {
                    captureSession?.addInput(input)
                    return true
                }
            } catch (e: Exception) {
                throw CameraException.ConfigurationError(e.message ?: "Unknown error")
            }
        }
        return false
    }
}