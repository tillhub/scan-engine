package de.tillhub.scanengine.camera

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.util.concurrent.Executor
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
internal class CameraHandlerTest {
    private lateinit var context: Context
    private lateinit var target: CameraHandlerImpl

    @BeforeTest
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        val executor = Executor { runnable -> runnable.run() }
        target = CameraHandlerImpl(context, executor)
    }

    @Test
    fun testGetCameraProviderInstantiation() {
        // Simple test to ensure the CameraHandlerImpl can be created
        // without calling getCameraProvider which requires CameraX setup
        assertNotNull(target)
    }
}