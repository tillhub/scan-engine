package de.tillhub.scanengine.theme

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

internal val TabletScaffoldModifier = Modifier
    .width(380.dp)
    .height(600.dp)
    .clip(RoundedCornerShape(8.dp))
