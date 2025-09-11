package de.tillhub.scanengine.camera

import de.tillhub.scanengine.camera.common.CaptureMetadataOutput
import de.tillhub.scanengine.camera.common.Dispatcher
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVMetadataObjectTypeAztecCode
import platform.AVFoundation.AVMetadataObjectTypeCode128Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Code
import platform.AVFoundation.AVMetadataObjectTypeCode39Mod43Code
import platform.AVFoundation.AVMetadataObjectTypeCode93Code
import platform.AVFoundation.AVMetadataObjectTypeDataMatrixCode
import platform.AVFoundation.AVMetadataObjectTypeEAN13Code
import platform.AVFoundation.AVMetadataObjectTypeEAN8Code
import platform.AVFoundation.AVMetadataObjectTypeITF14Code
import platform.AVFoundation.AVMetadataObjectTypePDF417Code
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.AVMetadataObjectTypeUPCECode
import kotlin.test.BeforeTest
import kotlin.test.Test

@ExperimentalForeignApi
internal class CameraControllerTest {
    lateinit var cameraWrapper: CameraWrapper
    lateinit var metadataOutput: CaptureMetadataOutput
    lateinit var analyzer: QRImageAnalyzer

    val dispatcher =
        object : Dispatcher {
            override fun dispatchAsync(
                priority: Int,
                block: () -> Unit,
            ) {
                block()
            }
        }

    lateinit var callbacks: Callbacks

    lateinit var target: CameraController

    @BeforeTest
    fun setup() {
        metadataOutput =
            mock {
                every { setMetadataObjectsDelegate(any(), any()) } returns Unit
                every { addMetadataObjectTypes(any()) } returns Unit
            }

        cameraWrapper =
            mock {
                every { setPreviewLayerFrame(any()) } returns Unit
                every { addOutputIfPossible(any()) } returns true
                every { setupPreviewLayer(any()) } returns Unit
                every { startSession() } returns Unit
                every { stopSession() } returns Unit
                every { setupSession() } returns Unit
                every { updateOrientation() } returns Unit
                every { isRunning() } returns true
                every { onError = any() } returns Unit
            }
        callbacks =
            mock {
                every { onBarcodeScanned(any()) } returns Unit
                every { onCameraError(any()) } returns Unit
            }
        analyzer = QRImageAnalyzer(callbacks::onBarcodeScanned)

        target =
            CameraController(
                cameraWrapper = cameraWrapper,
                metadataOutput = metadataOutput,
                onCameraError = callbacks::onCameraError,
                barcodeScanned = callbacks::onBarcodeScanned,
                analyzer = analyzer,
                dispatcher = dispatcher,
            )
    }

    @Test
    fun testViewDidLoad() {
        target.viewDidLoad()

        verify {
            cameraWrapper.onError = any()
            cameraWrapper.setupSession()
            cameraWrapper.setupPreviewLayer(any())
            cameraWrapper.addOutputIfPossible(metadataOutput)
            cameraWrapper.startSession()
        }
    }

    @Test
    fun testViewDidLayoutSubviews() {
        target.viewDidLayoutSubviews()

        verify {
            cameraWrapper.setPreviewLayerFrame(any())
            metadataOutput.setMetadataObjectsDelegate(
                delegate = any(),
                queue = any(),
            )
            cameraWrapper.isRunning()
            metadataOutput.addMetadataObjectTypes(
                types =
                listOf(
                    AVMetadataObjectTypeQRCode!!,
                    AVMetadataObjectTypeEAN13Code!!,
                    AVMetadataObjectTypeEAN8Code!!,
                    AVMetadataObjectTypeCode128Code!!,
                    AVMetadataObjectTypeCode39Code!!,
                    AVMetadataObjectTypeCode93Code!!,
                    AVMetadataObjectTypeCode39Mod43Code!!,
                    AVMetadataObjectTypeITF14Code!!,
                    AVMetadataObjectTypePDF417Code!!,
                    AVMetadataObjectTypeAztecCode!!,
                    AVMetadataObjectTypeDataMatrixCode!!,
                    AVMetadataObjectTypeUPCECode!!,
                ),
            )
        }
    }

    @Test
    fun testUpdateOrientation() {
        target.updateOrientation()

        verify {
            cameraWrapper.updateOrientation()
        }
    }

    @Test
    fun testStartSession() {
        target.startSession()

        verify {
            cameraWrapper.startSession()
        }
    }

    @Test
    fun testStopSession() {
        target.stopSession()

        verify {
            cameraWrapper.stopSession()
        }
    }

    interface Callbacks {
        fun onBarcodeScanned(barcode: String)

        fun onCameraError(error: String)
    }
}
