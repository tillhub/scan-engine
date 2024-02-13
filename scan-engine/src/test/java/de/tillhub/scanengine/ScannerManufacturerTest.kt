package de.tillhub.scanengine

import android.os.Build.MANUFACTURER
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk

class ScannerManufacturerTest : FunSpec({

    lateinit var build: android.os.Build
    beforeTest {
        build = mockk {
            every {
                MANUFACTURER
            } returns "SUNMI"
        }
    }
    test("addScanResult") {
        ScannerManufacturer.get() shouldBe ScannerManufacturer.OTHER
    }
})
