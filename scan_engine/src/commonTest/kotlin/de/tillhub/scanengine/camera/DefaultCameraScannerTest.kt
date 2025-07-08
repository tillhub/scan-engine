package de.tillhub.scanengine.camera

import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DefaultCameraScannerTest {
    private val testFlow = MutableStateFlow<ScannerEvent>(ScannerEvent.Camera.InProgress(null))

    private lateinit var target: DefaultCameraScanner

    @BeforeTest
    fun testSetup() {
        target = DefaultCameraScanner(testFlow)
    }

    @Test
    fun testScannerResults() = runTest {
        val result1 = target.observeScannerResults().firstOrNull()

        assertTrue(result1 is ScannerEvent.Camera.InProgress)
        assertNull(result1.scanKey)

        testFlow.value = ScannerEvent.ScanResult("test")

        val result2 = target.observeScannerResults().firstOrNull()

        assertTrue(result2 is ScannerEvent.ScanResult)
        assertNull(result2.scanKey)
        assertEquals("test", result2.value)
    }
}
