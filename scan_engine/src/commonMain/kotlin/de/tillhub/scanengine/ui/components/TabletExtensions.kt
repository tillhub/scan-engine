package de.tillhub.scanengine.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * This size represents minimum screen size to be considered as tablet
 * It also corresponds to values-w600dp folder in resources
 */
private const val TABLET_SCREEN_SIZE = 600

@Composable
internal fun isTablet(): Boolean {
    val configuration = LocalDensity.current
    val screenWidthDp = with(configuration) {
        LocalWindowInfo.current.containerSize.width / density
    }
    return screenWidthDp >= TABLET_SCREEN_SIZE
}

@Composable
internal fun getModifierBasedOnDeviceType(isTablet: Modifier, isMobile: Modifier): Modifier {
    return if (isTablet()) isTablet else isMobile
}
