package de.tillhub.scanengine.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.test.core.app.ApplicationProvider
import com.google.mlkit.vision.barcode.BarcodeScanner
import de.tillhub.scanengine.camera.common.CameraProvider
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(RobolectricTestRunner::class)
internal class CameraControllerTest {
    lateinit var context: Context
    lateinit var lifecycleOwner: LifecycleOwner
    lateinit var scanner: BarcodeScanner
    lateinit var analyzer: ImageAnalysis.Analyzer
    lateinit var executor: Executor
    lateinit var cameraHandler: CameraHandler

    lateinit var callbacks: Callbacks
    lateinit var cameraProvider: CameraProvider

    lateinit var target: CameraController

    @BeforeTest
    fun setup() {
        cameraProvider =
            mock {
                every { unbindAll() } returns Unit
                every { bindToLifecycle(any(), any(), any(), any()) } returns mock<Camera>()
            }
        callbacks =
            mock {
                every { onCameraReady() } returns Unit
                every { barcodeScanned(any()) } returns Unit
            }

        context = ApplicationProvider.getApplicationContext()
        lifecycleOwner = mock()
        scanner = mock()
        analyzer = mock()
        executor = mock()
        cameraHandler =
            mock {
                every { getCameraProvider(any()) } calls { (callback: (CameraProvider) -> Unit) ->
                    callback(cameraProvider)
                }
            }

        target =
            CameraController(
                context,
                lifecycleOwner,
                callbacks::barcodeScanned,
                scanner,
                analyzer,
                executor,
                cameraHandler,
            )
    }

    @Test
    fun testBindCamera() {
        val previewView = PreviewView(context)
        target.bindCamera(previewView, callbacks::onCameraReady)

        verify {
            cameraHandler.getCameraProvider(any())
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                any(),
                any(),
            )
            callbacks.onCameraReady()
        }
    }

    @Test
    fun testStopSession() {
        val previewView = PreviewView(context)
        target.bindCamera(previewView, callbacks::onCameraReady)

        target.stopSession()

        verify(VerifyMode.exactly(2)) {
            cameraProvider.unbindAll()
        }
    }

    interface Callbacks {
        fun barcodeScanned(barcode: String)

        fun onCameraReady()
    }
}
