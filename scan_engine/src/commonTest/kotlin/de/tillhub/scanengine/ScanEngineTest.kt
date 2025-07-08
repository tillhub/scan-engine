package de.tillhub.scanengine

import de.tillhub.scanengine.camera.DefaultCameraScanner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class ScanEngineTest {

    lateinit var scanEngine: ScanEngine

    @BeforeTest
    fun setup() {
        scanEngine = ScanEngine.getInstance()
    }

    @Test
    fun testCameraScanner() {
        val cameraScanner = scanEngine.newCameraScanner()

        assertTrue(cameraScanner is DefaultCameraScanner)
    }
}
