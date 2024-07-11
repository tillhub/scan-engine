package de.tillhub.scanengine.extensions

import android.annotation.SuppressLint
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

/**
 * This size represents minimum screen size to be considered as tablet
 * It also corresponds to values-w600dp folder in resources
 */
private const val TABLET_SCREEN_SIZE = 600

@Composable
internal fun isTablet(): Boolean {
    return LocalConfiguration.current.screenWidthDp >= TABLET_SCREEN_SIZE
}

@Composable
@SuppressLint("ModifierFactoryExtensionFunction")
internal fun getModifierBasedOnDeviceType(isTablet: Modifier, isMobile: Modifier): Modifier {
    return if (isTablet()) isTablet else isMobile
}
