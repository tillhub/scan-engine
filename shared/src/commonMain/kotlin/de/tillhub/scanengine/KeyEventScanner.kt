package de.tillhub.scanengine

import androidx.compose.ui.input.key.KeyEvent

interface KeyEventScanner {
    fun dispatchKeyEvent(event: KeyEvent, scanKey: String? = null)
}
