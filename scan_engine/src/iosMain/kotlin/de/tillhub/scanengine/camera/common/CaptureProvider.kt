package de.tillhub.scanengine.camera.common

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDevicePositionUnspecified
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVMediaTypeVideo
import platform.UIKit.UIView

internal interface CaptureProvider {
    fun getCaptureSession(): CaptureSession
    @ExperimentalForeignApi
    fun getCaptureVideoPreviewLayer(
        session: CaptureSession,
        view: UIView,
        orientation: AVCaptureVideoOrientation,
    ): CaptureVideoPreviewLayer
    fun getCaptureDevices(): List<CaptureDevice>
}

@ExperimentalForeignApi
internal object CaptureProviderImpl: CaptureProvider {
    override fun getCaptureSession(): CaptureSession = CaptureSessionImpl()

    override fun getCaptureVideoPreviewLayer(
        session: CaptureSession,
        view: UIView,
        orientation: AVCaptureVideoOrientation,
    ): CaptureVideoPreviewLayer = CaptureVideoPreviewLayerImpl(
        session = session,
        view = view,
        orientation = orientation,
    )

    override fun getCaptureDevices(): List<CaptureDevice> =
        AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
            AVMediaTypeVideo,
            AVCaptureDevicePositionUnspecified,
        ).devices.mapNotNull { it as? AVCaptureDevice }.map { CaptureDeviceImpl(it) }
}