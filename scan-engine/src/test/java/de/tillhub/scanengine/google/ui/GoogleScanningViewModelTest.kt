package de.tillhub.scanengine.google.ui

import androidx.camera.core.ImageInfo
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

@ExperimentalCoroutinesApi
class GoogleScanningViewModelTest : FunSpec({

    lateinit var scanner: BarcodeScanner
    lateinit var imageProxy: ImageProxy
    lateinit var inputImage: InputImage
    lateinit var inputImageGenerator: InputImageGenerator
    lateinit var imageInfo: ImageInfo
    lateinit var viewModel: GoogleScanningViewModel
    lateinit var task: Task<List<Barcode>>
    lateinit var testScope: TestScope

    beforeTest {
        testScope = TestScope(UnconfinedTestDispatcher())
        task = mockk()
        inputImage = mockk()
        imageInfo = mockk {
            every { rotationDegrees } returns 1
        }
        imageProxy = mockk {
            every { image } returns mockk()
            every { getImageInfo() } returns imageInfo
            every { close() } just Runs
        }
        inputImageGenerator = mockk {
            every { fromMediaImage(any(), any()) } returns inputImage
        }
        scanner = mockk {
            every { process(inputImage) } returns task
            every { close() } just Runs
        }
        viewModel = GoogleScanningViewModel(scanner, inputImageGenerator)
    }

    test("barcode scan updates state to CodeScanned") {
        val barcode = mockk<Barcode> {
            every { rawValue } returns "1234567890"
        }
        val analyzer = viewModel.analyzer as QRImageAnalyzer
        val testResults = mutableListOf<ScanningState>()

        every { task.addOnSuccessListener(any()) } answers {
            val arr = firstArg<OnSuccessListener<List<Barcode>>>()
            arr.onSuccess(listOf(barcode))
            task
        }
        every { task.addOnCompleteListener(any()) } answers {
            val arr = firstArg<OnCompleteListener<List<Barcode>>>()
            arr.onComplete(task)
            task
        }
        viewModel.scanningState.value shouldBe ScanningState.Idle

        testScope.launch {
            viewModel.scanningState.toList(testResults)
        }
        analyzer.analyze(imageProxy)
        val result = testResults.last()
        assertTrue(result is ScanningState.CodeScanned)
        assertEquals((result as ScanningState.CodeScanned).barcode, "1234567890")

        verify(ordering = Ordering.ORDERED) {
            scanner.close()
            imageProxy.close()
        }
    }
})
