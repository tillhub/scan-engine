package de.tillhub.scanengine.camera

import de.tillhub.scanengine.camera.CameraWrapperImpl.CameraException
import de.tillhub.scanengine.camera.common.CaptureMetadataOutput
import de.tillhub.scanengine.camera.common.CaptureProvider
import de.tillhub.scanengine.camera.common.CaptureProviderImpl
import de.tillhub.scanengine.camera.common.CaptureSession
import de.tillhub.scanengine.camera.common.CaptureVideoPreviewLayer
import de.tillhub.scanengine.camera.common.Dispatcher
import de.tillhub.scanengine.camera.common.DispatcherImpl
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeLeft
import platform.AVFoundation.AVCaptureVideoOrientationLandscapeRight
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.AVFoundation.AVCaptureVideoOrientationPortraitUpsideDown
import platform.UIKit.UIDevice
import platform.UIKit.UIDeviceOrientation
import platform.UIKit.UIView


@ExperimentalForeignApi
internal interface CameraWrapper {
    var onError: ((CameraException) -> Unit)?

    fun setupSession()
    fun startSession()
    fun stopSession()
    fun setupPreviewLayer(view: UIView)
    fun setPreviewLayerFrame(view: UIView)
    fun updateOrientation()
    fun isRunning(): Boolean?
    fun addOutputIfPossible(output: CaptureMetadataOutput): Boolean?
}

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
@OptIn(ExperimentalForeignApi::class)
internal class CameraWrapperImpl(
    private val captureFactory: CaptureProvider = CaptureProviderImpl,
    private val dispatcher: Dispatcher = DispatcherImpl
) : CameraWrapper {
    override var onError: ((CameraException) -> Unit)? = null

    private var cameraPreviewLayer: CaptureVideoPreviewLayer? = null
    private var captureSession: CaptureSession? = null

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
    override fun setupSession() {
        try {
            captureSession = captureFactory.getCaptureSession()
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
    override fun startSession() {
        if (captureSession?.isRunning() == false) {
            dispatcher.dispatchAsync {
                captureSession?.startRunning()
            }
        }
    }

    /**
     * Stops the camera capture session if it is currently running.
     */
    override fun stopSession() {
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
    override fun setupPreviewLayer(view: UIView) {
        captureSession?.let { session ->
            val newPreviewLayer = captureFactory.getCaptureVideoPreviewLayer(
                session = session,
                view = view,
                orientation = currentVideoOrientation(),
            )

            cameraPreviewLayer = newPreviewLayer
        }
    }
    
    /**
     * Sets the frame of the camera preview layer to match the bounds of a given UIView.
     *
     * This function is a convenience wrapper around [setPreviewLayerFrame] that takes a `UIView`
     * as input and uses its `bounds` to update the frame of the `cameraPreviewLayer`.
     * This is useful for ensuring the preview layer correctly fills the view it's displayed in.
     *
     * @param view The `UIView` whose bounds will be used to set the frame of the preview layer.
     */
    override fun setPreviewLayerFrame(view: UIView) {
        cameraPreviewLayer?.setFrame(view)
    }

    /**
     * Updates the orientation of the camera preview layer to match the current device orientation.
     *
     * This function calls [currentVideoOrientation] to get the appropriate video orientation
     * and then sets the `orientation` property of the `cameraPreviewLayer`. This ensures that
     * the camera preview is displayed correctly as the device is rotated.
     */
    override fun updateOrientation() {
        cameraPreviewLayer?.orientation = currentVideoOrientation()
    }

    /**
     * Checks if the camera capture session is currently running.
     *
     * @return `true` if the session is running, `false` if it's not, or `null` if the
     *         `captureSession` is not initialized.
     */
    override fun isRunning(): Boolean? = captureSession?.isRunning()
    
    /**
     * Attempts to add a `CaptureMetadataOutput` to the current `captureSession` if possible.
     *
     * This function delegates the operation to the `addOutputIfPossible` method of the `captureSession`.
     * It's used to add specific outputs to the session, but only if they are compatible and can be added.
     *
     * @param output The `CaptureMetadataOutput` to be added to the session.
     * @return `true` if the output was successfully added,
     *         `false` if it could not be added (e.g., due to incompatibility),
     *         or `null` if the `captureSession` is not initialized.
     */
    override fun addOutputIfPossible(output: CaptureMetadataOutput): Boolean? =
        captureSession?.addOutputIfPossible(output)

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
    private fun currentVideoOrientation(): AVCaptureVideoOrientation {
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
        val availableDevices = captureFactory.getCaptureDevices()

        availableDevices.find { it.position == AVCaptureDevicePositionBack }?.let { currentCamera ->
            try {
                if (captureSession?.addInputIfPossible(currentCamera) == true) {
                    return true
                }
            } catch (e: Exception) {
                throw CameraException.ConfigurationError(e.message ?: "Unknown error")
            }
        }
        return false
    }
}
