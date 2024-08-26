package de.tillhub.scanengine.zebra

import com.zebra.scannercontrol.SDKHandler
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.barcode.BarcodeScannerContainer
import de.tillhub.scanengine.common.ViewModelFunSpec
import de.tillhub.scanengine.data.ScanEvent
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

@ExperimentalCoroutinesApi
class ZebraPairBarcodeViewModelTest : ViewModelFunSpec({

    lateinit var scanEngine: ScanEngine
    lateinit var zebraScanner: ZebraBarcodeScanner
    lateinit var barcodeScannerContainer: BarcodeScannerContainer
    beforeTest {
        scanEngine = mockk<ScanEngine>(relaxed = true)
        zebraScanner = mockk<ZebraBarcodeScanner>(relaxed = true)
        barcodeScannerContainer = mockk<BarcodeScannerContainer>(relaxed = true)
        mockkObject(ScanEngine)
    }

    test("initial state is Loading") {
        every { scanEngine.barcodeScanner } returns barcodeScannerContainer
        every { scanEngine.observeScannerResults() } returns flowOf()
        every { barcodeScannerContainer.getScannersByType(ZebraBarcodeScanner::class.java) } returns zebraScanner

        val viewModel = ZebraPairBarcodeViewModel(scanEngine)
        viewModel.uiStateFlow.value shouldBe ZebraPairBarcodeViewModel.State.Loading
    }

    test("state transitions to Connected on ScanEvent.Connected") {
        val scanEvents = MutableStateFlow<ScanEvent>(ScanEvent.NotConnected)

        every { scanEngine.barcodeScanner } returns barcodeScannerContainer
        every { barcodeScannerContainer.getScannersByType(ZebraBarcodeScanner::class.java) } returns zebraScanner
        every { scanEngine.observeScannerResults() } returns scanEvents

        val viewModel = ZebraPairBarcodeViewModel(scanEngine)

        scanEvents.tryEmit(ScanEvent.Connected)

        viewModel.uiStateFlow.value shouldBe ZebraPairBarcodeViewModel.State.Connected
    }

    test("initScanner sets state to Pairing") {
        val result = mockk<Result<SDKHandler>>(relaxed = true)

        every { scanEngine.barcodeScanner } returns barcodeScannerContainer
        every { barcodeScannerContainer.getScannersByType(ZebraBarcodeScanner::class.java) } returns zebraScanner
        coEvery { zebraScanner.initScanner() } returns result

        val viewModel = ZebraPairBarcodeViewModel(scanEngine)

        viewModel.initScanner()

        viewModel.uiStateFlow.value shouldBe ZebraPairBarcodeViewModel.State.Pairing(result)
    }
})
