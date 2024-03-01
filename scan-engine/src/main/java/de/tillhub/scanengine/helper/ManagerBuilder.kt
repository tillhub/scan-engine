package de.tillhub.scanengine.helper

import androidx.lifecycle.Lifecycle
import androidx.savedstate.SavedStateRegistry

interface ManagerBuilder<T> {
    fun build(lifecycle: Lifecycle, savedStateRegistry: SavedStateRegistry): T
}
