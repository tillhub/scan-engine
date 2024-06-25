package de.tillhub.scanengine.data

import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.robolectric.shadows.ShadowBuild

@RobolectricTest
internal class ScannerManufacturerTest : FunSpec({

    test("SUNMI device") {
        ShadowBuild.setManufacturer("SUNMI")
        ScannerManufacturer.get() shouldBe ScannerManufacturer.SUNMI
    }

    test("NOT_SUNMI device") {
        ShadowBuild.setManufacturer("NOT_SUNMI")
        ScannerManufacturer.get() shouldBe ScannerManufacturer.NOT_SUNMI
    }
})
