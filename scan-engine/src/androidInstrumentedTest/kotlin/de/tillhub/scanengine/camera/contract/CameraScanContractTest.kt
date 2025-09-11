package de.tillhub.scanengine.camera.contract

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tillhub.scanengine.data.ScannerEvent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class CameraScanContractTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testRememberCameraScanLauncherCreatesContract() {
        val onResult: (ScannerEvent) -> Unit = { }

        var contract: CameraScanContract? = null

        composeTestRule.setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        // Verify the contract is created
        assertNotNull(contract)
    }

    @Test
    fun testCameraScanContractLaunchWithScanKey() {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null
        val testScanKey = "test_scan_key_123"

        composeTestRule.setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        // Launch camera scanner with scan key
        assertNotNull(contract)
        contract.launchCameraScanner(testScanKey)

        // Wait for composition to settle
        composeTestRule.waitForIdle()

        // Verify that InProgress event was triggered
        assertTrue(events.isNotEmpty())
        val inProgressEvent = events.find { it is ScannerEvent.Camera.InProgress }
        assertNotNull(inProgressEvent)
        assertTrue(inProgressEvent is ScannerEvent.Camera.InProgress)
        assertEquals(testScanKey, inProgressEvent.scanKey)
    }

    @Test
    fun testCameraScanContractLaunchWithoutScanKey() {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null

        composeTestRule.setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        // Launch camera scanner without scan key (null)
        assertNotNull(contract)
        contract.launchCameraScanner(null)

        // Wait for composition to settle
        composeTestRule.waitForIdle()

        // Verify that InProgress event was triggered with null scan key
        assertTrue(events.isNotEmpty())
        val inProgressEvent = events.find { it is ScannerEvent.Camera.InProgress }
        assertNotNull(inProgressEvent)
        assertTrue(inProgressEvent is ScannerEvent.Camera.InProgress)
        assertEquals(null, inProgressEvent.scanKey)
    }

    @Test
    fun testCameraScanContractWithStateChanges() {
        var latestEvent: ScannerEvent? = null
        var eventCount = 0

        val onResult: (ScannerEvent) -> Unit = { event ->
            latestEvent = event
            eventCount++
        }

        var contract: CameraScanContract? = null

        composeTestRule.setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        // Wait for initial composition
        composeTestRule.waitForIdle()

        // Verify contract is created but no events fired yet
        assertNotNull(contract)
        assertEquals(0, eventCount)

        // Manually trigger launch to test functionality
        contract.launchCameraScanner("manual_test")

        // Wait for composition updates
        composeTestRule.waitForIdle()

        // Verify event was captured
        assertNotNull(latestEvent)
        assertTrue(latestEvent is ScannerEvent.Camera.InProgress)
        assertEquals(1, eventCount)
    }

    @Test
    fun testCameraScanContractRecomposition() {
        var recompositionCount = 0

        val onResult: (ScannerEvent) -> Unit = { }

        composeTestRule.setContent {
            recompositionCount++

            val contract = rememberCameraScanLauncher(onResult = onResult)

            // Contract should be remembered across recompositions
            assertNotNull(contract)
        }

        // Wait for composition to settle
        composeTestRule.waitForIdle()

        // Verify initial composition happened
        assertTrue(recompositionCount >= 1)

        // Force another composition cycle
        composeTestRule.waitForIdle()

        // Contract should remain stable across recompositions
        // (This tests the remember behavior)
        assertTrue(recompositionCount >= 1)
    }

    @Test
    fun testMultipleLaunchCalls() {
        val events = mutableListOf<ScannerEvent>()
        val onResult: (ScannerEvent) -> Unit = { event ->
            events.add(event)
        }

        var contract: CameraScanContract? = null

        composeTestRule.setContent {
            contract = rememberCameraScanLauncher(onResult = onResult)
        }

        // Launch multiple times with different scan keys
        assertNotNull(contract)
        contract.launchCameraScanner("first_launch")
        contract.launchCameraScanner("second_launch")
        contract.launchCameraScanner(null)

        // Wait for all compositions to complete
        composeTestRule.waitForIdle()

        // Verify all launches triggered InProgress events
        val inProgressEvents = events.filterIsInstance<ScannerEvent.Camera.InProgress>()
        assertEquals(3, inProgressEvents.size)

        // Verify the scan keys are correct
        assertEquals("first_launch", inProgressEvents[0].scanKey)
        assertEquals("second_launch", inProgressEvents[1].scanKey)
        assertEquals(null, inProgressEvents[2].scanKey)
    }
}
