package de.tillhub.scanengine.camera.ui

import android.app.Activity
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class CameraScanActivityTest {

    @Test
    fun testDataKeyConstant() {
        // Test the constant without launching activity
        assertEquals("scanned_data", CameraScanActivity.DATA_KEY)
    }

    @Test
    fun testActivityLaunch() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, CameraScanActivity::class.java)
        
        ActivityScenario.launch<CameraScanActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
                
                // Test basic activity functionality
                val testBarcode = "test_barcode_123"
                val resultIntent = Intent().apply {
                    putExtra(CameraScanActivity.DATA_KEY, testBarcode)
                }
                
                // Verify we can set result and finish activity
                activity.setResult(Activity.RESULT_OK, resultIntent)
                activity.finish()
            }
        }
    }

    @Test
    fun testActivityCancellation() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = Intent(context, CameraScanActivity::class.java)
        
        ActivityScenario.launch<CameraScanActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull(activity)
                
                // Test cancellation scenario
                activity.setResult(Activity.RESULT_CANCELED)
                activity.finish()
            }
        }
    }
}