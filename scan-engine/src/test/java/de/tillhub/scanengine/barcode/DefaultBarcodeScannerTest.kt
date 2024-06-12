package de.tillhub.scanengine.barcode

import de.tillhub.scanengine.data.ScanEvent
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class DefaultBarcodeScannerTest : FunSpec({

    lateinit var mutableScanEvents: MutableSharedFlow<ScanEvent>
    lateinit var barcodeScanner: BarcodeScannerImpl
    lateinit var testScope: TestScope

    beforeTest {
        testScope = TestScope(UnconfinedTestDispatcher())
        mutableScanEvents = MutableSharedFlow(replay = 1)
        barcodeScanner = DefaultBarcodeScanner(mutableScanEvents)
    }

    test("observeScannerResults should return the current state flow") {
        val testResults = mutableListOf<ScanEvent>()
        testScope.launch {
            barcodeScanner.observeScannerResults().collect {
                testResults.add(it)
            }
        }
        mutableScanEvents.tryEmit(ScanEvent.Idle)
        testResults.first() shouldBe ScanEvent.Idle
    }
})
