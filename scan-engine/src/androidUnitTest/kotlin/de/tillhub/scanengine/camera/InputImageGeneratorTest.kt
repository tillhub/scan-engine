package de.tillhub.scanengine.camera

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(RobolectricTestRunner::class)
internal class InputImageGeneratorTest {
    private lateinit var target: InputImageGenerator

    @BeforeTest
    fun setup() {
        target = InputImageGenerator()
    }

    @Test
    fun testInputImageGeneratorInstantiates() {
        // Simple test to ensure the InputImageGenerator can be created
        assertNotNull(target)
    }
}