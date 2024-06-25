import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.app.ActivityOptionsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import de.tillhub.scanengine.R
import de.tillhub.scanengine.google.ui.GoogleScanningActivity
import org.hamcrest.CoreMatchers.not
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GoogleScanningActivityTest {

    private lateinit var scenario: ActivityScenario<GoogleScanningActivity>

    @After
    fun teardown() {
        scenario.close()
    }

    @Test
    fun testCameraPermissionGranted() {
        grantPermission()
        scenario = launchActivity<GoogleScanningActivity>()
        scenario.onActivity { activity ->
            val permissionStatus: Int = activity.checkSelfPermission(Manifest.permission.CAMERA)
            assert(permissionStatus == PackageManager.PERMISSION_GRANTED)
            assertTrue(activity.isCameraInitialized)
        }
        onView(withId(R.id.previewView)).check(matches(isDisplayed()))
        onView(withId(R.id.requestPermission)).check(matches(not(isDisplayed())))
    }

    @Test
    fun testCameraPermissionDenied() {
        revokePermission()
        scenario = launchActivity<GoogleScanningActivity>()
        scenario.onActivity { activity ->
            val permissionStatus: Int = activity.checkSelfPermission(Manifest.permission.CAMERA)
            assert(permissionStatus == PackageManager.PERMISSION_DENIED)
        }
    }

    private fun grantPermission() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.
        executeShellCommand("pm grant ${ApplicationProvider.getApplicationContext<Context>().packageName} ${Manifest.permission.CAMERA}")
    }
    private fun revokePermission() {
        InstrumentationRegistry.getInstrumentation().uiAutomation.
        executeShellCommand("pm revoke ${ApplicationProvider.getApplicationContext<Context>().packageName} ${Manifest.permission.CAMERA}")
    }
}
