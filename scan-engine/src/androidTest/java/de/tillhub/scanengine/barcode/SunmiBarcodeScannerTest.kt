package de.tillhub.scanengine.barcode


import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tillhub.scanengine.data.ScanEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SunmiBarcodeScannerTest {

    private lateinit var mutableScanEvents: MutableSharedFlow<ScanEvent>
    private lateinit var context: Context
    private lateinit var testScope: TestScope

    @Before
    fun setUp() {
        testScope = TestScope(UnconfinedTestDispatcher())
        context = ApplicationProvider.getApplicationContext()
        mutableScanEvents = MutableSharedFlow(replay = 1)
        SunmiBarcodeScanner(context, mutableScanEvents)
    }

    @Test
    @Throws(Exception::class)
    fun testBroadcastReceiverRegistered() {
        val intent = Intent("com.sunmi.scanner.ACTION_DATA_CODE_RECEIVED").apply {
            putExtra("data", "barcode")
        }

        val testResults = mutableListOf<ScanEvent>()

        testScope.launch {
            mutableScanEvents.collect { testResults.add(it) }
        }
        context.sendBroadcast(intent)
        val result = testResults.first()
        assertTrue(result is ScanEvent.Success)
        assertEquals((result as ScanEvent.Success).value, "barcode")
        assertNull(result.scanKey)
    }
}
