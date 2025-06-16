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
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow

actual class CameraController(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner,
    private val scannerEvents: MutableStateFlow<ScannerEvent>,
    scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
    )
) {
    private var scanKey: String? = null;

    private val analyzer: ImageAnalysis.Analyzer = QRImageAnalyzer(
        scanner = scanner,
        inputImageGenerator = InputImageGenerator()
    ) { barcode ->
        scannerEvents.value = ScannerEvent.ScanResult(barcode, scanKey)
        scanKey = null
    }

    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var camera: Camera? = null
    private var previewView: PreviewView? = null

    private val executor = ContextCompat.getMainExecutor(context)

    fun bindCamera(previewView: PreviewView, onCameraReady: () -> Unit = {}) {
        this.previewView = previewView

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                cameraProvider = cameraProviderFuture.get()
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
                    imageAnalyzer
                )

                onCameraReady()

            },
            executor
        )
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

    actual fun setScanKey(scanKey: String?) {
        this.scanKey = scanKey
    }


    private fun createResolutionSelector(): ResolutionSelector {
        return ResolutionSelector.Builder()
            .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
            .setAspectRatioStrategy(AspectRatioStrategy.RATIO_4_3_FALLBACK_AUTO_STRATEGY)
            .build()
    }
}

internal class QRImageAnalyzer(
    private val scanner: BarcodeScanner,
    private val inputImageGenerator: InputImageGenerator,
    private val barcodeScanned: (String) -> Unit
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = inputImageGenerator.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { list ->
                if (list.size > 0 && list[0].rawValue != null) {
                    barcodeScanned(list[0].rawValue!!)
                    scanner.close()
                }
            }.addOnCompleteListener {
                imageProxy.close()
            }
        }
    }
}

internal class InputImageGenerator {
    fun fromMediaImage(mediaImage: Image, rotationDegrees: Int): InputImage {
        return InputImage.fromMediaImage(mediaImage, rotationDegrees)
    }
}