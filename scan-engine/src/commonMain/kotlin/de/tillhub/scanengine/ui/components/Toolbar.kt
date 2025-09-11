@file:OptIn(ExperimentalMaterial3Api::class)

package de.tillhub.scanengine.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun Toolbar(
    title: String,
    onClick: () -> Unit,
) {
    Column {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors()
                .copy(containerColor = Color.White),
            title = {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.testTag("toolbarTitle"),
                )
            },
            navigationIcon = {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier.testTag("toolbarIcon"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "close",
                    )
                }
            },
        )
        HorizontalDivider()
    }
}

@Preview
@Composable internal fun ToolbarPreview() {
    Toolbar("Title example") {}
}
