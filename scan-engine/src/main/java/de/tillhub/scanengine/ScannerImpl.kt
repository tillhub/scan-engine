package de.tillhub.scanengine

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleOwner
import androidx.savedstate.SavedStateRegistry
import de.tillhub.scanengine.Scanner.Companion.PROVIDER
import de.tillhub.scanengine.Scanner.Companion.SCAN_KEY
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

abstract class ScannerImpl(
    private val savedStateRegistry: SavedStateRegistry,
    protected val mutableScanEvents: MutableSharedFlow<ScanEvent>
) : Scanner {

    protected var scanKey: String? = null
    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        if (savedStateRegistry.getSavedStateProvider(PROVIDER) == null) {
            savedStateRegistry.registerSavedStateProvider(PROVIDER, this)
        } else {
            scanKey = savedStateRegistry.consumeRestoredStateForKey(PROVIDER)
                ?.getString(SCAN_KEY)
        }
    }

    override fun saveState(): Bundle {
        return bundleOf(SCAN_KEY to scanKey)
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        savedStateRegistry.unregisterSavedStateProvider(PROVIDER)
    }

    override fun observeScannerResults(): SharedFlow<ScanEvent> = mutableScanEvents
}
