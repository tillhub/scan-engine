package de.tillhub.scanengine.defaulttest

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.default.DefaultScanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
class DefaultScannerTest : FunSpec({

    lateinit var componentActivity: ComponentActivity
    lateinit var defaultScanner: DefaultScanner
    lateinit var activityResultLauncher: ActivityResultLauncher<String>
    lateinit var registry: ActivityResultRegistry

    beforeTest {

        activityResultLauncher = mockk()

        registry = mockk {
            every {
                register(
                    any(),
                    any(),
                    any<ActivityResultContract<String, ScanEvent>>(),
                ) {}
            } returns activityResultLauncher
        }

        componentActivity = mockk {
            every { activityResultRegistry } returns registry
        }

        defaultScanner = DefaultScanner(componentActivity.activityResultRegistry)
    }

    test("test scanResults()") {
        val data = ScanEvent.Success("value", "key")

        var event: ScanEvent = ScanEvent.Canceled
        runTest(UnconfinedTestDispatcher()) {
            val collectJob = defaultScanner.observeScannerResults()
                .onEach { event = it }
                .launchIn(this)

            defaultScanner.scanEventProvider.addScanResult(data)
            collectJob.cancel()
        }

        event.shouldBeInstanceOf<ScanEvent.Success>()
        (event as ScanEvent.Success).value shouldBe data
    }
})
