@file:OptIn(ExperimentalMaterial3Api::class)

package de.tillhub.scanengine.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import de.tillhub.scanengine.resources.Res
import de.tillhub.scanengine.resources.navigate_back
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun Toolbar(
    title: String,
    onClick: () -> Unit,
) {
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
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(Res.string.navigate_back),
                )
            }
        },
    )
}

@Preview
@Composable internal fun ToolbarPreview() {
    Toolbar("Title example") {}
}
