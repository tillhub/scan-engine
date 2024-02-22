package de.tillhub.scanengine

import SingletonHolder
import androidx.activity.ComponentActivity
import androidx.lifecycle.Lifecycle
import de.tillhub.scanengine.default.DefaultScanner
import de.tillhub.scanengine.sunmi.SunmiScanner

class ScanEngine private constructor(activity: ComponentActivity) {

    private val scanner: Scanner by lazy {
        when (ScannerManufacturer.get()) {
            ScannerManufacturer.SUNMI -> SunmiScanner(activity)
            ScannerManufacturer.OTHER -> DefaultScanner(activity)
        }
    }

    fun attach(lifecycle: Lifecycle): Scanner {
        lifecycle.addObserver(scanner)
        return scanner
    }

    companion object : SingletonHolder<ScanEngine, ComponentActivity>(::ScanEngine)
}
