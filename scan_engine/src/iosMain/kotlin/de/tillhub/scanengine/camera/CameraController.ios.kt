package de.tillhub.scanengine.camera

import kotlinx.atomicfu.atomic
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureVideoOrientation
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
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
import platform.UIKit.UIViewController
import platform.darwin.NSObject
import platform.darwin.dispatch_get_main_queue

actual class CameraController(
    barcodeScanned: (String) -> Unit,
) : UIViewController(null, null) {
    private val analyzer: QRImageAnalyzer = QRImageAnalyzer(barcodeScanned)

    private val customCameraController = CustomCameraController()
    private var metadataOutput = AVCaptureMetadataOutput()

    /**
     * Starts the camera session.
     */
    actual fun startSession() {
        customCameraController.startSession()
        setupScanner()
    }

    /**
     * Stops the camera session.
     */
    actual fun stopSession() {
        customCameraController.stopSession()
    }

    override fun viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
    }

    fun getCameraPreviewLayer() = customCameraController.cameraPreviewLayer

    internal fun currentVideoOrientation(): AVCaptureVideoOrientation =
        customCameraController.currentVideoOrientation()

    private fun setupCamera() {
        customCameraController.setupSession()
        customCameraController.setupPreviewLayer(view)

        if (customCameraController.captureSession?.canAddOutput(metadataOutput) == true) {
            customCameraController.captureSession?.addOutput(metadataOutput)
        }

        startSession()

        customCameraController.onError = { error ->
            println("Camera Error: $error")
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        customCameraController.cameraPreviewLayer?.setFrame(view.bounds)
    }

    private fun setupScanner() {
        metadataOutput.setMetadataObjectsDelegate(analyzer, dispatch_get_main_queue())

//        metadataOutput.metadataObjectTypes += listOf(
//            AVMetadataObjectTypeQRCode!!,
//            AVMetadataObjectTypeEAN13Code!!,
//            AVMetadataObjectTypeEAN8Code!!,
//            AVMetadataObjectTypeCode128Code!!,
//            AVMetadataObjectTypeCode39Code!!,
//            AVMetadataObjectTypeCode93Code!!,
//            AVMetadataObjectTypeCode39Mod43Code!!,
//            AVMetadataObjectTypeITF14Code!!,
//            AVMetadataObjectTypePDF417Code!!,
//            AVMetadataObjectTypeAztecCode!!,
//            AVMetadataObjectTypeDataMatrixCode!!,
//            AVMetadataObjectTypeUPCECode!!
//        )
    }
}

internal class QRImageAnalyzer(
    private val onCodeScanned: (String) -> Unit,
    private val debounceMs: Long = 1000L
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    private val isProcessing = atomic(false)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastScannedCode: String? = null
    private var debounceJob: Job? = null

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        if (isProcessing.value) return

        didOutputMetadataObjects.firstOrNull {
            it is AVMetadataMachineReadableCodeObject &&
                    !it.stringValue.isNullOrEmpty() &&
                    it.stringValue != lastScannedCode
        }?.let { scannedCode ->
            (scannedCode as AVMetadataMachineReadableCodeObject).stringValue?.let {
                processCode(it)
            }
        }
    }

    private fun processCode(code: String) {
        debounceJob?.cancel()
        debounceJob = scope.launch {
            if (isProcessing.compareAndSet(expect = false, update = true)) {
                try {
                    lastScannedCode = code
                    onCodeScanned(code)
                    delay(debounceMs)
                } finally {
                    isProcessing.value = false
                }
            }
        }
    }
}