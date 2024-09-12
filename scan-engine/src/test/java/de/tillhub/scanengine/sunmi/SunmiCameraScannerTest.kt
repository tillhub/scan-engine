import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.sunmi.camera.SunmiCameraScanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class SunmiCameraScannerTest : FunSpec({

    lateinit var resultCaller: ActivityResultCaller
    lateinit var mutableScannerEvents: MutableStateFlow<ScannerEvent>
    lateinit var scannerLauncher: ActivityResultLauncher<Unit>
    lateinit var sunmiCameraScanner: CameraScanner
    lateinit var testScope: TestScope

    beforeTest {
        val callbackSlot = slot<ActivityResultCallback<List<ScannerEvent>>>()

        mutableScannerEvents = MutableStateFlow(ScannerEvent.External.NotConnected)
        scannerLauncher = mockk<ActivityResultLauncher<Unit>>(relaxed = true)
        resultCaller = mockk<ActivityResultCaller>(relaxed = true) {
            every {
                registerForActivityResult<Unit, List<ScannerEvent>>(
                    any(),
                    capture(callbackSlot)
                )
            } returns scannerLauncher
        }
        testScope = TestScope(UnconfinedTestDispatcher())
        sunmiCameraScanner = SunmiCameraScanner(resultCaller, mutableScannerEvents)
    }

    test("startCameraScanner should emit InProgress event and launch scanner") {
        sunmiCameraScanner.startCameraScanner("testScanKey")

        mutableScannerEvents.value.shouldBeInstanceOf<ScannerEvent.Camera.InProgress>()
        (mutableScannerEvents.value as ScannerEvent.Camera.InProgress).scanKey shouldBe "testScanKey"
        verify { scannerLauncher.launch(Unit) }
    }

    test("registerForActivityResult callback emits ScanEvent_Canceled") {
        val callbackSlot = slot<ActivityResultCallback<List<ScannerEvent>>>()
        every {
            resultCaller.registerForActivityResult<Unit, List<ScannerEvent>>(any(), capture(callbackSlot))
        } returns scannerLauncher

        sunmiCameraScanner = SunmiCameraScanner(resultCaller, mutableScannerEvents)

        val events = listOf(ScannerEvent.Camera.Canceled)
        callbackSlot.captured.onActivityResult(events)

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            sunmiCameraScanner.observeScannerResults().toList(testResults)
        }
        sunmiCameraScanner.startCameraScanner()
        testResults.first() shouldBe ScannerEvent.Camera.Canceled
    }

    test("scannerLauncher should emit Success event") {
        val callbackSlot = slot<ActivityResultCallback<List<ScannerEvent>>>()
        every {
            resultCaller.registerForActivityResult<Unit, List<ScannerEvent>>(any(), capture(callbackSlot))
        } returns scannerLauncher

        sunmiCameraScanner = SunmiCameraScanner(resultCaller, mutableScannerEvents)

        val events = listOf(ScannerEvent.Success("123456789", "key"))
        callbackSlot.captured.onActivityResult(events)

        val testResults = mutableListOf<ScannerEvent>()
        testScope.launch {
            sunmiCameraScanner.observeScannerResults().toList(testResults)
        }
        sunmiCameraScanner.startCameraScanner("key")
        val result = testResults.first()
        result.shouldBeInstanceOf<ScannerEvent.Success>()
        result.value shouldBe "123456789"
    }
})
