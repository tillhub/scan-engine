package de.tillhub.scanengine.google.ui

import android.media.Image
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class GoogleScanningViewModel(
    scanner: BarcodeScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS).build()
    ),
    inputImageGenerator: InputImageGenerator = InputImageGenerator()
) : ViewModel() {

    private val _scanningState: MutableStateFlow<ScanningState> = MutableStateFlow(ScanningState.Idle)
    val scanningState: StateFlow<ScanningState> = _scanningState

    val analyzer: ImageAnalysis.Analyzer = QRImageAnalyzer(scanner, _scanningState, inputImageGenerator)

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                GoogleScanningViewModel()
            }
        }
    }
}

internal class QRImageAnalyzer(
    private val scanner: BarcodeScanner,
    private val scanningState: MutableStateFlow<ScanningState>,
    private val inputImageGenerator: InputImageGenerator
) : ImageAnalysis.Analyzer {

    @androidx.camera.core.ExperimentalGetImage
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = inputImageGenerator.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image).addOnSuccessListener { list ->
                if (list.size > 0 && list[0].rawValue != null) {
                    scanningState.value = ScanningState.CodeScanned(list[0].rawValue!!)
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

internal sealed class ScanningState {
    data object Idle : ScanningState()
    data class CodeScanned(val barcode: String) : ScanningState()
}
