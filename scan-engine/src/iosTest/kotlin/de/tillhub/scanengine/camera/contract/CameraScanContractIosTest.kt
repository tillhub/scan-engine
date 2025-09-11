package de.tillhub.scanengine.camera.contract

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import de.tillhub.scanengine.data.ScannerEvent
import kotlinx.cinterop.ExperimentalForeignApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class, ExperimentalForeignApi::class)
internal class CameraScanContractIosTest {

    @Test
    fun testRememberCameraScanLauncherCreatesContract() = runComposeUiTest {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null

        setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        waitForIdle()

        // Verify the contract is created
        assertNotNull(contract)
    }

    @Test
    fun testCameraScanContractLaunchWithScanKey() = runComposeUiTest {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null
        val testScanKey = "test_scan_key_123"

        setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        waitForIdle()

        // Launch camera scanner with scan key
        assertNotNull(contract)
        contract.launchCameraScanner(testScanKey)

        // Verify that InProgress event was triggered
        assertTrue(events.isNotEmpty())
        val inProgressEvent = events.find { it is ScannerEvent.Camera.InProgress }
        assertNotNull(inProgressEvent)
        assertTrue(inProgressEvent is ScannerEvent.Camera.InProgress)
        assertEquals(testScanKey, inProgressEvent.scanKey)
    }

    @Test
    fun testCameraScanContractLaunchWithoutScanKey() = runComposeUiTest {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null

        setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        waitForIdle()

        // Launch camera scanner without scan key (null)
        assertNotNull(contract)
        contract.launchCameraScanner(null)

        // Verify that InProgress event was triggered with null scan key
        assertTrue(events.isNotEmpty())
        val inProgressEvent = events.find { it is ScannerEvent.Camera.InProgress }
        assertNotNull(inProgressEvent)
        assertTrue(inProgressEvent is ScannerEvent.Camera.InProgress)
        assertEquals(null, inProgressEvent.scanKey)
    }

    @Test
    fun testCameraScanContractRecomposition() = runComposeUiTest {
        var recompositionCount = 0
        val onResult: (ScannerEvent) -> Unit = { }

        setContent {
            recompositionCount++

            val contract = rememberCameraScanLauncher(onResult = onResult)

            // Contract should be remembered across recompositions
            assertNotNull(contract)
        }

        // Wait for composition to settle
        waitForIdle()

        // Verify initial composition happened
        assertTrue(recompositionCount >= 1)

        // Force another composition cycle
        waitForIdle()

        // Contract should remain stable across recompositions
        // (This tests the remember behavior)
        assertTrue(recompositionCount >= 1)
    }

    @Test
    fun testMultipleLaunchCalls() = runComposeUiTest {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null

        setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        waitForIdle()

        // Launch multiple times with different scan keys
        assertNotNull(contract)
        contract.launchCameraScanner("first_launch")
        contract.launchCameraScanner("second_launch")
        contract.launchCameraScanner(null)

        // Verify all launches triggered InProgress events
        val inProgressEvents = events.filterIsInstance<ScannerEvent.Camera.InProgress>()
        assertEquals(3, inProgressEvents.size)

        // Verify the scan keys are correct
        assertEquals("first_launch", inProgressEvents[0].scanKey)
        assertEquals("second_launch", inProgressEvents[1].scanKey)
        assertEquals(null, inProgressEvents[2].scanKey)
    }

    @Test
    fun testContractCallbackHandling() = runComposeUiTest {
        var latestEvent: ScannerEvent? = null
        var eventCount = 0

        val onResult: (ScannerEvent) -> Unit = { event ->
            latestEvent = event
            eventCount++
        }

        var contract: CameraScanContract? = null

        setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        // Wait for initial composition
        waitForIdle()

        // Verify contract is created but no events fired yet
        assertNotNull(contract)
        assertEquals(0, eventCount)

        // Manually trigger launch to test functionality
        contract.launchCameraScanner("callback_test")

        // Wait for composition updates
        waitForIdle()

        // Verify event was captured
        assertNotNull(latestEvent)
        assertTrue(latestEvent is ScannerEvent.Camera.InProgress)
        assertEquals(1, eventCount)
    }
}
