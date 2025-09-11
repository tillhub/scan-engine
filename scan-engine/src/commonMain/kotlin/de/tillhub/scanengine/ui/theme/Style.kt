package de.tillhub.scanengine.ui.theme

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults.buttonElevation
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun buttonElevation() = buttonElevation(
    defaultElevation = 3.dp,
    pressedElevation = 1.dp,
    disabledElevation = 1.dp,
    hoveredElevation = 2.dp,
    focusedElevation = 2.dp,
)

@Composable
internal fun textFieldTransparentColors() = TextFieldDefaults.colors(
    unfocusedContainerColor = Color.Transparent,
    focusedContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
)

internal val TabletScaffoldModifier =
    Modifier
        .width(380.dp)
        .height(600.dp)
        .clip(RoundedCornerShape(8.dp))
