package de.tillhub.scanengine.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
internal fun AppTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        typography = typography,
        content = content,
    )
}
