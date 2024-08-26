package de.tillhub.scanengine.google

import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultCaller
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.data.ScanEvent
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
class DefaultCameraScannerTest : FunSpec({

    lateinit var resultCaller: ActivityResultCaller
    lateinit var mutableScanEvents: MutableStateFlow<ScanEvent>
    lateinit var scannerLauncher: ActivityResultLauncher<Unit>
    lateinit var defaultCameraScanner: CameraScanner
    lateinit var testScope: TestScope

    beforeTest {
        val callbackSlot = slot<ActivityResultCallback<ScanEvent>>()

        mutableScanEvents = MutableStateFlow(ScanEvent.NotConnected)
        scannerLauncher = mockk<ActivityResultLauncher<Unit>>(relaxed = true)
        resultCaller = mockk<ActivityResultCaller>(relaxed = true) {
            every {
                registerForActivityResult<Unit, ScanEvent>(
                    any(),
                    capture(callbackSlot)
                )
            } returns scannerLauncher
        }
        testScope = TestScope(UnconfinedTestDispatcher())
        defaultCameraScanner = DefaultCameraScanner(resultCaller, mutableScanEvents)
    }

    test("startCameraScanner should emit InProgress event and launch scanner") {
        defaultCameraScanner.startCameraScanner("testScanKey")

        mutableScanEvents.value.shouldBeInstanceOf<ScanEvent.InProgress>()
        (mutableScanEvents.value as ScanEvent.InProgress).scanKey shouldBe "testScanKey"
        verify { scannerLauncher.launch(Unit) }
    }

    test("registerForActivityResult callback emits ScanEvent_Canceled") {
        val callbackSlot = slot<ActivityResultCallback<ScanEvent>>()
        every {
            resultCaller.registerForActivityResult<Unit, ScanEvent>(any(), capture(callbackSlot))
        } returns scannerLauncher

        defaultCameraScanner = DefaultCameraScanner(resultCaller, mutableScanEvents)

        val events = ScanEvent.Canceled
        callbackSlot.captured.onActivityResult(events)

        val testResults = mutableListOf<ScanEvent>()
        testScope.launch {
            defaultCameraScanner.observeScannerResults().toList(testResults)
        }
        defaultCameraScanner.startCameraScanner()
        testResults.first() shouldBe ScanEvent.Canceled
    }

    test("scannerLauncher should emit Success event") {
        val callbackSlot = slot<ActivityResultCallback<ScanEvent>>()
        every {
            resultCaller.registerForActivityResult<Unit, ScanEvent>(any(), capture(callbackSlot))
        } returns scannerLauncher

        defaultCameraScanner = DefaultCameraScanner(resultCaller, mutableScanEvents)

        val events = ScanEvent.Success("123456789", "key")
        callbackSlot.captured.onActivityResult(events)

        val testResults = mutableListOf<ScanEvent>()
        testScope.launch {
            defaultCameraScanner.observeScannerResults().toList(testResults)
        }
        defaultCameraScanner.startCameraScanner("key")
        val result = testResults.first()
        result.shouldBeInstanceOf<ScanEvent.Success>()
        result.value shouldBe "123456789"
    }
})
