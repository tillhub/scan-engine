package de.tillhub.scanengine.barcode

import android.bluetooth.BluetoothManager
import android.content.Context
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.ScannerType
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
    lateinit var mutableScannerEvents: MutableStateFlow<ScannerEvent>
    lateinit var externalScanners: List<ScannerType>
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
        mutableScannerEvents = MutableStateFlow(mockk(relaxed = true))
        externalScanners = listOf(ScannerType.ZEBRA)
        scannerFactory = mockk()
    }

    test("getScannersByType success") {
        val container = BarcodeScannerContainer(context, mutableScannerEvents, externalScanners)
        val zebraScanner = container.getScannersByType(ZebraBarcodeScanner::class.java)
        zebraScanner.shouldBeInstanceOf<ZebraBarcodeScanner>()
    }
    test("getScannersByType error") {
        val type = ZebraBarcodeScanner::class.java
        val container = BarcodeScannerContainer(context, mutableScannerEvents, emptyList())
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
            externalScanners = externalScanners,
            scannerFactory = scannerFactory
        )
        val testResults = mutableListOf<ScannerEvent>()
        val event = ScannerEvent.Success("value")
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
            externalScanners,
            scannerFactory = scannerFactory
        )
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
            externalScanners,
            scannerFactory = scannerFactory
        )
        container.startPairingScreen(ScannerType.ZEBRA)
        verify { zebraScanner.startPairingScreen(ScannerType.ZEBRA) }
    }
})
