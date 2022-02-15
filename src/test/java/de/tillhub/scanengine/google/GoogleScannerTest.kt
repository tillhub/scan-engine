package de.tillhub.scanengine.google


import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import de.tillhub.scanengine.ScannerConnection
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.ScannedData
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest

@ExperimentalCoroutinesApi
class GoogleScannerTest : FunSpec({

    lateinit var componentActivity: ComponentActivity
    lateinit var activity: Activity
    lateinit var appContext: Context
    lateinit var googleScanner: GoogleScanner

    lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    beforeTest {
        activityResultLauncher = mockk()
        componentActivity = mockk {
            every { registerForActivityResult<Intent, ActivityResult>(any(), any()) } returns activityResultLauncher
        }
        activity = mockk()
        appContext = mockk()

        googleScanner = GoogleScanner(appContext)
    }

    test("test connect()") {
        val connNull = googleScanner.connect(activity)
        val conn = googleScanner.connect(componentActivity)

        connNull shouldBe null
        conn shouldNotBe null
        conn.shouldBeInstanceOf<ScannerConnection>()
    }


    test("test disconnect()") {
        val conn: ScannerConnection = mockk {
            every { disconnect() } just Runs
        }

        googleScanner.disconnect(conn)

        verify(exactly = 1) { conn.disconnect() }
    }

    test("test scanResults()") {
        val data = ScannedData("value", "key")

        var event: ScanEvent = ScanEvent.Error()
        runTest(UnconfinedTestDispatcher()) {
            val collectJob = googleScanner.scanResults()
                .onEach { event = it }
                .launchIn(this)

            googleScanner.scanEventProvider.addScanResult(data)
            collectJob.cancel()
        }

        event.shouldBeInstanceOf<ScanEvent.Success>()
        (event as ScanEvent.Success).content shouldBe data
    }

    test("test scanCameraCode()") {
        val conn: GoogleScannerConnection = mockk {
            every { scanCameraCode(any()) } just Runs
        }

        googleScanner.activeScannerConnection = conn
        googleScanner.scanCameraCode(false, "key")

        verify(exactly = 1) { conn.scanCameraCode("key") }
    }
})
