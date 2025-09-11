@file:OptIn(ExperimentalTestApi::class)

package de.tillhub.scanengine.ui.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.tillhub.scanengine.ui.theme.AppTheme
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class ToolbarTest {

    @Test
    fun toolbarDisplaysTitleCorrectly() = runComposeUiTest {
        var clicks = 0
        val testTitle = "Test Title"

        setContent {
            AppTheme {
                Toolbar(
                    title = testTitle,
                    onClick = {
                        clicks++
                    },
                )
            }
        }

        onNodeWithContentDescription("close").assertExists()
        onNodeWithTag("toolbarTitle").assertTextEquals(testTitle)

        onNodeWithTag("toolbarIcon").performClick()

        assertEquals("onClick should be called once", 1, clicks)
    }
}
