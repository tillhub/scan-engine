package de.tillhub.scanengine.camera

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
import platform.darwin.DISPATCH_QUEUE_PRIORITY_HIGH
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

class CameraWrapper: NSObject() {
    private var currentCamera: AVCaptureDevice? = null
    var captureSession: AVCaptureSession? = null
    var cameraPreviewLayer: AVCaptureVideoPreviewLayer? = null

    var onError: ((CameraException) -> Unit)? = null

    sealed class CameraException : Exception() {
        class DeviceNotAvailable : CameraException()
        class ConfigurationError(message: String) : CameraException()
    }

    fun setupSession() {
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

    fun startSession() {
        if (captureSession?.isRunning() == false) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH.toLong(), 0u)) {
                captureSession?.startRunning()
            }
        }
    }

    fun stopSession() {
        if (captureSession?.isRunning() == true) {
            captureSession?.stopRunning()
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun setupPreviewLayer(view: UIView) {
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

    fun currentVideoOrientation(): AVCaptureVideoOrientation {
        val orientation = UIDevice.currentDevice.orientation
        return when (orientation) {
            UIDeviceOrientation.UIDeviceOrientationPortrait -> AVCaptureVideoOrientationPortrait
            UIDeviceOrientation.UIDeviceOrientationPortraitUpsideDown -> AVCaptureVideoOrientationPortraitUpsideDown
            UIDeviceOrientation.UIDeviceOrientationLandscapeLeft -> AVCaptureVideoOrientationLandscapeRight
            UIDeviceOrientation.UIDeviceOrientationLandscapeRight -> AVCaptureVideoOrientationLandscapeLeft
            else -> AVCaptureVideoOrientationPortrait
        }
    }

    private fun cleanupSession() {
        stopSession()
        cameraPreviewLayer?.removeFromSuperlayer()
        cameraPreviewLayer = null
        captureSession = null
        currentCamera = null
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException", "ReturnCount")
    @OptIn(ExperimentalForeignApi::class)
    private fun setupInputs(): Boolean {
        val availableDevices = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
            AVMediaTypeVideo,
            AVCaptureDevicePositionUnspecified
        ).devices

        if (availableDevices.isEmpty()) return false

        val backCamera = availableDevices.find {
            (it as AVCaptureDevice).position == AVCaptureDevicePositionBack
        } as? AVCaptureDevice

        //val backCamera = availableDevices.firstOrNull() as? AVCaptureDevice

        currentCamera = backCamera ?: return false

        try {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(
                currentCamera!!,
                null
            ) ?: return false

            if (captureSession?.canAddInput(input) == true) {
                captureSession?.addInput(input)
                return true
            }
        } catch (e: Exception) {
            throw CameraException.ConfigurationError(e.message ?: "Unknown error")
        }
        return false
    }
}