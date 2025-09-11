package de.tillhub.scanengine.ui

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tillhub.scanengine.ui.components.Toolbar
import de.tillhub.scanengine.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ToolbarInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun toolbarDisplaysTitleCorrectly() {
        val testTitle = "Test Title"

        composeTestRule.setContent {
            AppTheme {
                Toolbar(
                    title = testTitle,
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("toolbarTitle").assertTextEquals(testTitle)
    }

    @Test
    fun toolbarClickHandlerTest() {
        var clicks = 0

        composeTestRule.setContent {
            AppTheme {
                Toolbar(
                    title = "Sample Title",
                    onClick = {
                        clicks++
                    },
                )
            }
        }

        composeTestRule.onNodeWithTag("toolbarIcon").performClick()

        assertEquals("onClick should be called once", 1, clicks)
    }

    @Test
    fun toolbarIconContentDescriptionTest() {
        composeTestRule.setContent {
            AppTheme {
                Toolbar(
                    title = "Test Title",
                    onClick = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("close").assertExists()
    }
}
