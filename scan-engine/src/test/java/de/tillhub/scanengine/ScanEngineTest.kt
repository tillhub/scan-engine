package de.tillhub.scanengine

import android.app.Activity
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.activity.result.ActivityResultCaller
import de.tillhub.scanengine.barcode.BarcodeScannerContainer
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.generic.GenericKeyEventScanner
import de.tillhub.scanengine.google.DefaultCameraScanner
import de.tillhub.scanengine.sunmi.camera.SunmiCameraScanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
class ScanEngineTest : FunSpec({

    lateinit var context: Context
    lateinit var activity: Activity
    lateinit var resultCaller: ActivityResultCaller
    lateinit var scanEngine: ScanEngine

    beforeTest {
        mockkObject(ScannerType)
        context = mockk(relaxed = true) {
            every {
                getSystemService(Context.BLUETOOTH_SERVICE)
            } returns mockk<BluetoothManager>(relaxed = true)
        }
        activity = mockk(relaxed = true)
        resultCaller = mockk(relaxed = true)
        scanEngine = ScanEngine.getInstance(context).initBarcodeScanners(ScannerType.ZEBRA)
    }

    test("newCameraScanner should return SunmiCameraScanner for SUNMI scanner type") {
        every { ScannerType.get() } returns ScannerType.SUNMI

        val scanner = scanEngine.newCameraScanner(resultCaller)
        scanner.shouldBeInstanceOf<SunmiCameraScanner>()
    }

    test("newCameraScanner should return DefaultCameraScanner for other scanner types") {
        every { ScannerType.get() } returns ScannerType.UNKNOWN

        val scanner = scanEngine.newCameraScanner(resultCaller)
        scanner.shouldBeInstanceOf<DefaultCameraScanner>()
    }

    test("barcodeScanner lazy initialization should initialize BarcodeScannerContainer correctly") {
        val barcodeScanner = scanEngine.barcodeScanner
        barcodeScanner.shouldBeInstanceOf<BarcodeScannerContainer>()
    }

    test("newKeyEventScanner") {
        val scanner = scanEngine.newKeyEventScanner(activity)
        scanner.shouldBeInstanceOf<GenericKeyEventScanner>()
    }
})
