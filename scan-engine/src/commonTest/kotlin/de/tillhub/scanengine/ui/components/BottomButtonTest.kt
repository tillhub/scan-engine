@file:OptIn(ExperimentalTestApi::class)

package de.tillhub.scanengine.ui.components

import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import de.tillhub.scanengine.ui.theme.AppTheme
import kotlin.test.DefaultAsserter.assertEquals
import kotlin.test.Test

class BottomButtonTest {

    @Test
    fun submitButtonTest() = runComposeUiTest {
        var clicks = 0

        setContent {
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

        onNodeWithTag("submitButton").performClick()
        onNodeWithContentDescription("Bottom button label").assertTextEquals("Submit")

        assertEquals("onClick should be called once", 1, clicks)
    }
}
