package de.tillhub.scanengine.barcode

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.generic.GenericKeyEventScanner
import de.tillhub.scanengine.sunmi.barcode.SunmiBarcodeScanner
import de.tillhub.scanengine.zebra.ZebraBarcodeScanner
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class BarcodeScannerContainerTest : FunSpec({

    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var mutableScannerEvents: MutableStateFlow<ScannerEvent>
    lateinit var testScope: TestScope
    lateinit var scannerFactory: BarcodeScannerFactory

    beforeTest {
        mockkObject(ScannerType)
        testScope = TestScope(UnconfinedTestDispatcher())
        context = mockk(relaxed = true) {
            every {
                getSystemService(Context.BLUETOOTH_SERVICE)
            } returns mockk<BluetoothManager>(relaxed = true)
        }
        activity = mockk(relaxed = true)
        mutableScannerEvents = MutableStateFlow(mockk(relaxed = true))
        scannerFactory = mockk()
    }

    test("getScannersByType success") {
        val container = BarcodeScannerContainer(context, mutableScannerEvents)
        container.addScanner(ScannerType.ZEBRA)
        val zebraScanner = container.getScannersByType(ZebraBarcodeScanner::class.java)
        zebraScanner.shouldBeInstanceOf<ZebraBarcodeScanner>()
    }
    test("addScanner") {
        val container = BarcodeScannerContainer(context, mutableScannerEvents)
        val scanner = GenericKeyEventScanner(activity, mutableScannerEvents)
        container.addScanner(scanner)
        val genericScanner = container.getScannersByType(GenericKeyEventScanner::class.java)
        genericScanner.shouldBeInstanceOf<GenericKeyEventScanner>()
    }
    test("getScannersByType error") {
        val type = ZebraBarcodeScanner::class.java
        val container = BarcodeScannerContainer(context, mutableScannerEvents)
        val exception = shouldThrow<NoSuchElementException> {
            container.getScannersByType(type)
        }
        exception.message shouldBe "No scanner found of type $type"
    }

    test("observeScannerResults") {
        every { ScannerType.get() } returns ScannerType.SUNMI
        val zebraScanner: ZebraBarcodeScanner = mockk(relaxed = true) {
            every { observeScannerResults() } returns mutableScannerEvents
        }

        val sunmiScanner: SunmiBarcodeScanner = mockk(relaxed = true) {
            every { observeScannerResults() } returns mutableScannerEvents
        }
        every { scannerFactory.getZebraBarcodeScanner(any(), any()) } returns zebraScanner
        every { scannerFactory.getSunmiBarcodeScanner(any(), any()) } returns sunmiScanner

        val container = BarcodeScannerContainer(
            context = context,
            mutableScannerEvents = mutableScannerEvents,
            scannerFactory = scannerFactory
        )
        container.addScanner(ScannerType.ZEBRA)
        val testResults = mutableListOf<ScannerEvent>()
        val event = ScannerEvent.ScanResult("value")
        mutableScannerEvents.tryEmit(event)
        testScope.launch {
            container.observeScannerResults().toList(testResults)
        }
        testResults shouldBe listOf(event, event)
        verify {
            zebraScanner.observeScannerResults()
            sunmiScanner.observeScannerResults()
        }
    }

    test("scanWithKey") {
        val zebraScanner = mockk<ZebraBarcodeScanner>(relaxed = true)
        every { scannerFactory.getZebraBarcodeScanner(any(), any()) } returns zebraScanner

        val container = BarcodeScannerContainer(
            context,
            mutableScannerEvents,
            scannerFactory = scannerFactory
        )
        container.addScanner(ScannerType.ZEBRA)
        container.scanWithKey("test_key")
        verify {
            zebraScanner.scanWithKey("test_key")
        }
    }

    test("startPairingScreen") {
        val zebraScanner = mockk<ZebraBarcodeScanner>(relaxed = true)
        every { scannerFactory.getZebraBarcodeScanner(any(), any()) } returns zebraScanner

        val container = BarcodeScannerContainer(
            context,
            mutableScannerEvents,
            scannerFactory = scannerFactory
        )
        container.addScanner(ScannerType.ZEBRA)
        container.startPairingScreen(ScannerType.ZEBRA)
        verify { zebraScanner.startPairingScreen(ScannerType.ZEBRA) }
    }
})
