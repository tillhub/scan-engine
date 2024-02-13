package de.tillhub.scanengine

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.bundleOf
import androidx.savedstate.SavedStateRegistry
import de.tillhub.scanengine.default.DefaultScanner
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalStdlibApi
@ExperimentalCoroutinesApi
class DefaultScannerTest : FunSpec({

    lateinit var componentActivity: ComponentActivity
    lateinit var defaultScanner: DefaultScanner
    lateinit var activityResultLauncher: ActivityResultLauncher<String>
    lateinit var resultRegistry: ActivityResultRegistry
    lateinit var savedRegistry: SavedStateRegistry
    val scanProvider = ScanEventProvider()

    beforeTest {
        activityResultLauncher = mockk {
            every { launch(any()) } just Runs
        }
        savedRegistry = mockk {
            every { registerSavedStateProvider(any(), any()) } just Runs
            every { consumeRestoredStateForKey(any()) } returns bundleOf()
            every { consumeRestoredStateForKey(any())?.getString(any()) } returns "scan-key"
        }
        resultRegistry = mockk {
            every {
                register(
                    any(),
                    any(),
                    any<ActivityResultContract<String, ScanEvent>>(),
                    any()
                )
            } returns activityResultLauncher
        }

        componentActivity = mockk {
            every { activityResultRegistry } returns resultRegistry
            every { savedStateRegistry } returns savedRegistry
        }

        defaultScanner = DefaultScanner(componentActivity, scanProvider)
    }

    test("test scanResults()").config(coroutineTestScope = true) {
        val data = ScanEvent.Success("value", "key")
        var event: ScanEvent = ScanEvent.Canceled

        val collectJob = defaultScanner.observeScannerResults()
            .onEach { event = it }
            .launchIn(TestScope(UnconfinedTestDispatcher()))
        scanProvider.addScanResult(data)
        collectJob.cancel()

        event shouldBe data
    }

    test("test startCameraScanner()") {
        defaultScanner.onCreate(componentActivity)
        defaultScanner.startCameraScanner("scan-key")
        verify {
            activityResultLauncher.launch("scan-key")
        }
    }
})
