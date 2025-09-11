package de.tillhub.scanengine.camera.common

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.UIKit.UIView

internal interface CaptureVideoPreviewLayer {
    var orientation: Long?

    fun setFrame(view: UIView)

    fun removeFromSuperlayer()
}

@ExperimentalForeignApi
internal class CaptureVideoPreviewLayerImpl(
    session: CaptureSession,
    view: UIView,
    orientation: AVCaptureVideoOrientation,
) : CaptureVideoPreviewLayer {
    private val avCaptureVideoPreviewLayer =
        AVCaptureVideoPreviewLayer(
            session = session.getSession(),
        ).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
            setFrame(view.bounds)
            connection?.videoOrientation = orientation

            view.layer.addSublayer(this)
        }

    override var orientation: Long?
        get() = avCaptureVideoPreviewLayer.connection?.videoOrientation
        set(value) {
            value ?: return
            avCaptureVideoPreviewLayer.connection?.videoOrientation = value
        }

    override fun setFrame(view: UIView) {
        avCaptureVideoPreviewLayer.setFrame(view.bounds)
    }

    override fun removeFromSuperlayer() = avCaptureVideoPreviewLayer.removeFromSuperlayer()
}
