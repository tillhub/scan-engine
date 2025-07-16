package de.tillhub.scanengine.camera

import android.content.Context
import android.media.Image
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.AspectRatioStrategy
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import de.tillhub.scanengine.camera.common.CameraProvider
import java.util.concurrent.Executor

/**
 * Manages the camera operations for barcode scanning.
 *
 * This class is responsible for setting up the camera preview, handling the image analysis
 * for barcode detection, and managing the camera lifecycle. It uses CameraX for camera
 * operations and ML Kit for barcode scanning.
 *
 * @property context The application context.
 * @property lifecycleOwner The [LifecycleOwner] to which the camera lifecycle will be bound.
 *                          This ensures that camera resources are managed correctly according to the
 *                          lifecycle of the component (e.g., Activity or Fragment) using the camera.
 * @property barcodeScanned A lambda function that is invoked when a barcode is successfully scanned.
 *                          It receives the raw value of the scanned barcode as a [String].
 * @property scanner The [BarcodeScanner] instance used for detecting barcodes. By default, it's
 *                   configured to scan all barcode formats.
 */
@Suppress("LongParameterList")
internal actual class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    barcodeScanned: (String) -> Unit,
    scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build(),
    ),
    private val analyzer: ImageAnalysis.Analyzer = QRImageAnalyzer(
        scanner = scanner,
        inputImageGenerator = InputImageGenerator(),
        barcodeScanned = barcodeScanned,
    ),
    private val executor: Executor = ContextCompat.getMainExecutor(context),
    private val cameraHandler: CameraHandler = CameraHandlerImpl(context, executor)
) {

    private var cameraProvider: CameraProvider? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var previewView: PreviewView? = null

    /**
     * Binds the camera to the provided [PreviewView] and starts the camera session.
     *
     * This function initializes the camera provider, sets up the preview and image analysis
     * use cases, and binds them to the lifecycle of the [lifecycleOwner].
     *
     * @param previewView The [PreviewView] where the camera preview will be displayed.
     * @param onCameraReady A callback function that is invoked when the camera has been successfully
     *                      bound and is ready to display the preview. This is an optional parameter
     *                      and defaults to an empty function.
     */
    internal fun bindCamera(previewView: PreviewView, onCameraReady: () -> Unit = {}) {
        this.previewView = previewView

        cameraHandler.getCameraProvider { provider ->
            cameraProvider = provider
            cameraProvider?.unbindAll()

            preview = Preview.Builder()
                .setResolutionSelector(createResolutionSelector())
                .build()
                .also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(executor, analyzer)
                }

            camera = cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalyzer,
            )

            onCameraReady()
        }
    }

    /**
     * Starts the camera session.
     */
    actual fun startSession() = Unit

    /**
     * Stops the camera session.
     */
    actual fun stopSession() {
        cameraProvider?.unbindAll()
    }

    /**
     * Creates a [ResolutionSelector] with predefined settings.
     *
     * This function configures the resolution selector to:
     * - Use the highest available resolution ([ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY]).
     * - Attempt to use a 4:3 aspect ratio, falling back to an automatically selected ratio if 4:3 is not available
     *   ([AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY]).
     *
     * @return A configured [ResolutionSelector] instance.
     */
    private fun createResolutionSelector(): ResolutionSelector {
        return ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .build()
    }
}

/**
 * An image analyzer for detecting and processing QR codes from a camera preview.
 * This class uses ML Kit's BarcodeScanner to find QR codes in the provided image frames.
 *
 * @property scanner The [BarcodeScanner] instance used for detecting barcodes.
 * @property inputImageGenerator A helper class to convert [ImageProxy] to [InputImage] for the scanner.
 * @property barcodeScanned A lambda function that is invoked when a barcode is successfully scanned,
 *                          passing the raw value of the barcode as a String.
 */
internal class QRImageAnalyzer(
    private val scanner: BarcodeScanner,
    private val inputImageGenerator: InputImageGenerator,
    private val barcodeScanned: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    /**
     * Analyzes an image from the camera preview to detect barcodes.
     * This method is called by the camera framework for each new frame.
     *
     * It converts the [ImageProxy] to an [InputImage] format suitable for ML Kit's BarcodeScanner.
     * If a barcode is detected, the [barcodeScanned] callback is invoked with the raw value
     * of the barcode, and the scanner is closed.
     *
     * Regardless of whether a barcode is found or not, [ImageProxy.close] is called to release
     * the image buffer and allow the camera to capture the next frame.
     *
     * @param imageProxy The image to be analyzed, provided by the camera framework.
     */
    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = inputImageGenerator.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { list ->
                if (list.isNotEmpty() && list[0].rawValue != null) {
                    barcodeScanned(list[0].rawValue!!)
                    scanner.close()
                }
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}

/**
 * A utility class for creating [InputImage] objects from Android [Image] objects.
 * This is used to prepare images from the camera for processing by ML Kit's BarcodeScanner.
 */
internal class InputImageGenerator {
    fun fromMediaImage(mediaImage: Image, rotationDegrees: Int): InputImage {
        return InputImage.fromMediaImage(mediaImage, rotationDegrees)
    }
}
