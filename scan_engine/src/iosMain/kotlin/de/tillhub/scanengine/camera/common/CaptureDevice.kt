package de.tillhub.scanengine.camera.common

import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePosition
import platform.AVFoundation.position

internal interface CaptureDevice {
    val position: AVCaptureDevicePosition
    fun getAVCaptureDeviceInput(): AVCaptureDeviceInput?
}

class CaptureDeviceImpl(
    private val avCaptureDevice: AVCaptureDevice
) : CaptureDevice {
    override val position = avCaptureDevice.position

    @ExperimentalForeignApi
    override fun getAVCaptureDeviceInput(): AVCaptureDeviceInput? =
        AVCaptureDeviceInput.deviceInputWithDevice(
            avCaptureDevice,
            null,
        )
}