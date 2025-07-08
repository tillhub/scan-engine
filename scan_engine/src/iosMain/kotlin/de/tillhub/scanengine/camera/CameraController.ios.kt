package de.tillhub.scanengine.camera

import de.tillhub.scanengine.camera.common.dispatchAsync
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

/**
 * iOS implementation of the CameraController.
 * This class is responsible for managing the camera session, preview layer, and barcode scanning.
 *
 * @param cameraWrapper An instance of [CameraWrapper] to interact with the camera hardware.
 * @param metadataOutput An instance of [AVCaptureMetadataOutput] to handle metadata from the camera,
 *                       specifically for barcode scanning.
 * @param onCameraError A lambda function that is invoked when a camera error occurs.
 *                      It receives the error message as a [String].
 * @param barcodeScanned A lambda function that is invoked when a barcode is successfully scanned.
 *                       It receives the scanned barcode value as a [String].
 */
actual class CameraController(
    private val cameraWrapper: CameraWrapper = CameraWrapper(),
    private val metadataOutput: AVCaptureMetadataOutput = AVCaptureMetadataOutput(),
    private val onCameraError: (String) -> Unit,
    barcodeScanned: (String) -> Unit,
) : UIViewController(null, null) {
    private val analyzer: QRImageAnalyzer = QRImageAnalyzer(barcodeScanned)

    /**
     * Called after the controller's view is loaded into memory.
     * This method initializes the camera setup.
     */
    override fun viewDidLoad() {
        super.viewDidLoad()
        setupCamera()
    }

    /**
     * Called to notify the view controller that its view has just laid out its subviews.
     * This method adjusts the camera preview layer's frame to match the view's bounds
     * and sets up the scanner asynchronously.
     */
    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLayoutSubviews() {
        super.viewDidLayoutSubviews()
        cameraWrapper.cameraPreviewLayer?.setFrame(view.bounds)
        dispatchAsync {
            setupScanner()
        }
    }

    /**
     * Provides access to the camera preview layer.
     * This layer is responsible for displaying the camera feed.
     *
     * @return The [platform.AVFoundation.AVCaptureVideoPreviewLayer] used for displaying the camera preview,
     *         or null if it's not available.
     */
    internal fun getCameraPreviewLayer() = cameraWrapper.cameraPreviewLayer

    /**
     * Starts the camera session.
     */
    actual fun startSession() {
        cameraWrapper.startSession()
    }

    /**
     * Stops the camera session.
     */
    actual fun stopSession() {
        cameraWrapper.stopSession()
    }

    /**
     * Retrieves the current video orientation from the camera wrapper.
     * This is important for ensuring that the displayed video and scanned barcodes
     * are correctly oriented according to the device's current orientation.
     *
     * @return The current [AVCaptureVideoOrientation] of the camera.
     */
    internal fun currentVideoOrientation(): AVCaptureVideoOrientation =
        cameraWrapper.currentVideoOrientation()

    /**
     * Sets up the camera by configuring the camera wrapper and metadata output.
     * This function initializes the error handling for the camera, sets up the camera session
     * and preview layer, adds the metadata output to the session if possible, and then starts
     * the camera session.
     *
     * - The `onError` callback of the [cameraWrapper] is set to print the error and invoke
     *   the [onCameraError] lambda passed to the constructor.
     * - [CameraWrapper.setupSession] is called to prepare the camera session.
     * - [CameraWrapper.setupPreviewLayer] is called to link the camera preview to the view.
     * - If the [cameraWrapper]'s capture session can accept the [metadataOutput], it is added.
     * - Finally, [startSession] is called to begin capturing video.
     */
    private fun setupCamera() {
        cameraWrapper.onError = { error ->
            println("Camera Error: $error")
            onCameraError(error.message.orEmpty())
        }

        cameraWrapper.setupSession()
        cameraWrapper.setupPreviewLayer(view)

        if (cameraWrapper.captureSession?.canAddOutput(metadataOutput) == true) {
            cameraWrapper.captureSession?.addOutput(metadataOutput)
        }

        startSession()
    }

    /**
     * Sets up the scanner by configuring the metadata output.
     * This function sets the delegate for handling metadata objects to the [analyzer]
     * and specifies the types of metadata objects (barcodes) to detect.
     * The metadata object types are only added if the camera session is currently running.
     *
     * The delegate will receive callbacks on the main dispatch queue.
     *
     * Supported barcode types include:
     * - QR Code
     * - EAN-13
     * - EAN-8
     * - Code 128
     * - Code 39
     * - Code 93
     * - Code 39 Mod 43
     * - ITF14
     * - PDF417
     * - Aztec
     * - Data Matrix
     * - UPC-E
     */
    private fun setupScanner() {
        metadataOutput.setMetadataObjectsDelegate(analyzer, dispatch_get_main_queue())

        if (cameraWrapper.captureSession?.isRunning() == true) {
            metadataOutput.metadataObjectTypes += listOf(
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
            )
        }
    }
}

/**
 * An internal class responsible for analyzing camera output for QR codes and other machine-readable codes.
 * It implements [AVCaptureMetadataOutputObjectsDelegateProtocol] to receive metadata objects from the camera.
 *
 * This class includes a debouncing mechanism to prevent processing the same code multiple times in quick succession.
 *
 * @property onCodeScanned A lambda function that is invoked when a new code is successfully scanned and processed.
 *                         It receives the scanned code value as a [String].
 * @property debounceMs The debounce time in milliseconds. If a new code is scanned within this time period
 *                      after the last processed code, it will be ignored to prevent rapid firing of the
 *                      [onCodeScanned] callback. Defaults to 1000 milliseconds (1 second).
 */
internal class QRImageAnalyzer(
    private val onCodeScanned: (String) -> Unit,
    private val debounceMs: Long = 1000L,
) : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {

    private val isProcessing = atomic(false)
    private val scope = CoroutineScope(Dispatchers.Main)
    private var lastScannedCode: String? = null
    private var debounceJob: Job? = null

    /**
     * Delegate method called when the capture output outputs new metadata objects.
     * This method is the entry point for barcode scanning. It processes the detected
     * metadata objects, filters for machine-readable codes, and ensures that the same code
     * is not processed repeatedly in quick succession.
     *
     * If a new, valid barcode is detected and the analyzer is not currently processing,
     * it calls [processCode] to handle the scanned code.
     *
     * @param output The capture output that produced the metadata objects.
     * @param didOutputMetadataObjects An array of metadata objects. Each object in this array
     *                                 is an instance of a subclass of [platform.AVFoundation.AVMetadataObject].
     * @param fromConnection The capture connection from which the metadata objects originated.
     */
    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection,
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

    /**
     * Processes a scanned code string.
     *
     * This function implements a debouncing mechanism to prevent rapid processing of the same code.
     * When a new code is received:
     * 1. Any existing debounce job is cancelled.
     * 2. A new coroutine is launched.
     * 3. It attempts to set the `isProcessing` flag to `true`. If successful:
     *     a. The `lastScannedCode` is updated to the new code.
     *     b. The `onCodeScanned` callback is invoked with the new code.
     *     c. A delay is introduced, equal to `debounceMs`.
     *     d. Finally, the `isProcessing` flag is reset to `false`.
     *
     * This ensures that `onCodeScanned` is not called too frequently if the camera detects the same
     * code multiple times in quick succession.
     *
     * @param code The scanned code string to process.
     */
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
