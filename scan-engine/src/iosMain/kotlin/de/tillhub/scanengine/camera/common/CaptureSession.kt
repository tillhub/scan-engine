package de.tillhub.scanengine.camera.common

import platform.AVFoundation.AVCaptureSession

internal interface CaptureSession {
    var sessionPreset: String?

    fun beginConfiguration()

    fun commitConfiguration()

    fun isRunning(): Boolean

    fun startRunning()

    fun stopRunning()

    fun addInputIfPossible(device: CaptureDevice): Boolean

    fun addOutputIfPossible(output: CaptureMetadataOutput): Boolean

    fun canAddOutput(output: CaptureMetadataOutput): Boolean

    fun addOutput(output: CaptureMetadataOutput)

    fun getSession(): AVCaptureSession
}

internal class CaptureSessionImpl : CaptureSession {
    private val session: AVCaptureSession = AVCaptureSession()

    override var sessionPreset: String?
        get() = session.sessionPreset
        set(value) {
            session.sessionPreset = value
        }

    override fun beginConfiguration() = session.beginConfiguration()

    override fun commitConfiguration() = session.commitConfiguration()

    override fun isRunning() = session.isRunning()

    override fun startRunning() = session.startRunning()

    override fun stopRunning() = session.stopRunning()

    override fun addInputIfPossible(device: CaptureDevice): Boolean {
        val input = device.getAVCaptureDeviceInput()

        if (input != null && session.canAddInput(input)) {
            session.addInput(input)
            return true
        }
        return false
    }

    override fun addOutputIfPossible(output: CaptureMetadataOutput): Boolean {
        if (session.canAddOutput(output.output)) {
            session.addOutput(output.output)
            return true
        }
        return false
    }

    override fun canAddOutput(output: CaptureMetadataOutput) = session.canAddOutput(output.output)

    override fun addOutput(output: CaptureMetadataOutput) = session.addOutput(output.output)

    override fun getSession() = session
}
