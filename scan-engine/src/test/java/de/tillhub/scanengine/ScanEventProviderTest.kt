package de.tillhub.scanengine

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
class ScanEventProviderTest : FunSpec({

    lateinit var scanEventProvider: ScanEventProvider

    val data = ScannedDataResult.ScannedData("value", "key")

    beforeTest {
        scanEventProvider = ScanEventProvider()
    }

    test("addScanResult") {
        var event: ScanEvent = ScanEvent.Error()
        runTest(UnconfinedTestDispatcher()) {
            val collectJob = scanEventProvider.scanEvents
                    .onEach { event = it }
                    .launchIn(this)

            scanEventProvider.addScanResult(data)
            collectJob.cancel()
        }

        event.shouldBeInstanceOf<ScanEvent.Success>()
        (event as ScanEvent.Success).content shouldBe data
    }
})
