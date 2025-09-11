package de.tillhub.scanengine.ui

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.tillhub.scanengine.ui.components.BottomButton
import de.tillhub.scanengine.ui.theme.AppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BottomButtonInstrumentedTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun submitButtonTest() {
        var clicks = 0

        composeTestRule.setContent {
            AppTheme {
                BottomButton(
                    isEnable = true,
                    modifier = Modifier.testTag("submitButton"),
                    text = "Submit",
                    onClick = {
                        clicks++
                    },
                )
            }
        }

        composeTestRule.onNodeWithTag("submitButton").performClick()
        composeTestRule.onNodeWithContentDescription("Bottom button label").assertTextEquals("Submit")

        assertEquals("onClick should be called once", 1, clicks)
    }
}
