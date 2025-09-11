package de.tillhub.scanengine.camera

import de.tillhub.scanengine.camera.common.CaptureDevice
import de.tillhub.scanengine.camera.common.CaptureMetadataOutput
import de.tillhub.scanengine.camera.common.CaptureProvider
import de.tillhub.scanengine.camera.common.CaptureSession
import de.tillhub.scanengine.camera.common.CaptureVideoPreviewLayer
import de.tillhub.scanengine.camera.common.Dispatcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetPhoto
import platform.AVFoundation.AVCaptureVideoOrientationPortrait
import platform.UIKit.UIView
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
internal class CameraWrapperTest {
    private lateinit var captureSession: CaptureSession
    private lateinit var captureVideoPreviewLayer: CaptureVideoPreviewLayer
    private lateinit var captureDevice: CaptureDevice

    private lateinit var captureProvider: CaptureProvider

    val dispatcher =
        object : Dispatcher {
            override fun dispatchAsync(
                priority: Int,
                block: () -> Unit,
            ) {
                block()
            }
        }

    private lateinit var target: CameraWrapper

    @BeforeTest
    fun setup() {
        captureSession =
            mock {
                every { sessionPreset = any() } returns Unit
                every { beginConfiguration() } returns Unit
                every { commitConfiguration() } returns Unit
                every { isRunning() } returns true
                every { startRunning() } returns Unit
                every { stopRunning() } returns Unit
                every { addInputIfPossible(any()) } returns true
                every { addOutputIfPossible(any()) } returns true
                every { canAddOutput(any()) } returns true
                every { addOutput(any()) } returns Unit
                every { getSession() } returns AVCaptureSession()
            }
        captureVideoPreviewLayer =
            mock {
                every { orientation } returns AVCaptureVideoOrientationPortrait
                every { orientation = any() } returns Unit
                every { setFrame(any()) } returns Unit
                every { removeFromSuperlayer() } returns Unit
            }
        captureDevice =
            mock {
                every { position } returns AVCaptureDevicePositionBack
            }

        captureProvider =
            mock {
                every { getCaptureSession() } returns captureSession
                every { getCaptureVideoPreviewLayer(any(), any(), any()) } returns captureVideoPreviewLayer
                every { getCaptureDevices() } returns listOf(captureDevice)
            }

        target =
            CameraWrapperImpl(
                captureFactory = captureProvider,
                dispatcher = dispatcher,
            )
    }

    @Test
    fun testSetupSession() {
        target.setupSession()

        verify {
            captureProvider.getCaptureSession()
            captureSession.beginConfiguration()
            captureSession.sessionPreset = AVCaptureSessionPresetPhoto
            captureProvider.getCaptureDevices()
            captureDevice.position
            captureSession.addInputIfPossible(any())
        }
    }

    @Test
    fun testStartSession() {
        target.setupSession()
        every { captureSession.isRunning() } returns false
        target.startSession()

        verify {
            captureSession.isRunning()
            captureSession.startRunning()
        }
    }

    @Test
    fun testStopSession() {
        target.setupSession()
        target.stopSession()

        verify {
            captureSession.isRunning()
            captureSession.stopRunning()
        }
    }

    @Test
    fun testSetupPreviewLayer() {
        target.setupSession()

        val view = UIView()

        target.setupPreviewLayer(view)

        verify {
            captureProvider.getCaptureVideoPreviewLayer(
                session = captureSession,
                view = view,
                orientation = AVCaptureVideoOrientationPortrait,
            )
        }
    }

    @Test
    fun testSetPreviewLayerFrame() {
        target.setupSession()
        val view = UIView()
        target.setupPreviewLayer(view)

        target.setPreviewLayerFrame(view)

        verify {
            captureVideoPreviewLayer.setFrame(view)
        }
    }

    @Test
    fun testUpdateOrientation() {
        target.setupSession()
        val view = UIView()
        target.setupPreviewLayer(view)

        target.updateOrientation()

        verify {
            captureVideoPreviewLayer.orientation = AVCaptureVideoOrientationPortrait
        }
    }

    @Test
    fun testIsRunning() {
        target.setupSession()
        val result = target.isRunning()

        assertNotNull(result)
        assertTrue(result)

        verify {
            captureSession.isRunning()
        }
    }

    @Test
    fun testAddOutputIfPossible() {
        target.setupSession()
        val output = mock<CaptureMetadataOutput>()
        val result = target.addOutputIfPossible(output)

        assertNotNull(result)
        assertTrue(result)

        verify {
            captureSession.addOutputIfPossible(output)
        }
    }
}
