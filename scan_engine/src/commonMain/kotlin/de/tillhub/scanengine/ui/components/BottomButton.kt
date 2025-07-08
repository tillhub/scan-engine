package de.tillhub.scanengine.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import de.tillhub.scanengine.ui.theme.MagneticGrey
import de.tillhub.scanengine.ui.theme.OrbitalBlue
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
internal fun BottomButton(
    isEnable: Boolean = true,
    text: String,
    onClick: () -> Unit = {},
) {
    Button(
        enabled = isEnable,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("submitButton"),
        shape = RectangleShape,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isEnable) OrbitalBlue else MagneticGrey,
        ),
    ) {
        Text(
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp),
            text = text,
        )
    }
}
