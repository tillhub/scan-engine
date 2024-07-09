package de.tillhub.scanengine.data

import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.robolectric.shadows.ShadowBuild

@RobolectricTest
internal class ScannerManufacturerTest : FunSpec({

    test("SUNMI device") {
        ShadowBuild.setManufacturer("SUNMI")
        ScannerType.get() shouldBe ScannerType.SUNMI
    }

    test("UNKNOWN device") {
        ShadowBuild.setManufacturer("UNKNOWN")
        ScannerType.get() shouldBe ScannerType.UNKNOWN
    }
})
