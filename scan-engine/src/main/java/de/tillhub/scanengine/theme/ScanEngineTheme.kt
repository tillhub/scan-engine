package de.tillhub.scanengine.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun ScanEngineTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Toolbar(
    title: String,
    onActionClick: () -> Unit
) {
    Column {
        TopAppBar(
            title = {
                Text(text = title)
            },
            navigationIcon = {
                IconButton(onClick = onActionClick) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Navigation action back")
                }
            }
        )
    }
}

@Preview
@Composable internal fun ToolbarPreview() {
    Toolbar("Title example") {}
}
