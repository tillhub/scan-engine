package de.tillhub.scanengine

import android.view.KeyEvent

interface KeyEventScanner {
    fun dispatchKeyEvent(event: KeyEvent, scanKey: String? = null)
}
