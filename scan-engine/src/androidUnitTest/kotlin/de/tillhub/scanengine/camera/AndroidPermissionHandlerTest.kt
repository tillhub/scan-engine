package de.tillhub.scanengine.camera

import android.Manifest
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
internal class AndroidPermissionHandlerTest {
    private lateinit var context: Context
    private lateinit var target: AndroidPermissionHandler

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        target = AndroidPermissionHandler(context)
    }

    @Test
    fun testInstantiation() {
        assertNotNull(target)
    }

    @Test
    fun testHasCameraPermissionWithGrantedPermission() {
        val shadowApplication = Shadows.shadowOf(context.applicationContext as android.app.Application)
        shadowApplication.grantPermissions(Manifest.permission.CAMERA)

        val result = target.hasCameraPermission()

        assertTrue(result)
    }

    @Test
    fun testHasCameraPermissionWithDeniedPermission() {
        val shadowApplication = Shadows.shadowOf(context.applicationContext as android.app.Application)
        shadowApplication.denyPermissions(Manifest.permission.CAMERA)

        val result = target.hasCameraPermission()

        assertFalse(result)
    }
}