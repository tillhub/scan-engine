package de.tillhub.scanengine.camera.common

import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.darwin.NSObject

internal interface CaptureMetadataOutput {
    val output: AVCaptureMetadataOutput

    fun setMetadataObjectsDelegate(
        delegate: AVCaptureMetadataOutputObjectsDelegateProtocol,
        queue: NSObject?,
    )

    fun addMetadataObjectTypes(types: List<String>)
}

internal class CaptureMetadataOutputImpl(
    private val metadataOutput: AVCaptureMetadataOutput = AVCaptureMetadataOutput(),
) : CaptureMetadataOutput {
    override val output = metadataOutput

    override fun setMetadataObjectsDelegate(
        delegate: AVCaptureMetadataOutputObjectsDelegateProtocol,
        queue: NSObject?,
    ) = metadataOutput.setMetadataObjectsDelegate(delegate, queue)

    override fun addMetadataObjectTypes(types: List<String>) {
        metadataOutput.metadataObjectTypes += types
    }
}
