package de.tillhub.scanengine

import androidx.activity.ComponentActivity
import de.tillhub.scanengine.google.GoogleScanner
import de.tillhub.scanengine.helper.SingletonHolder
import de.tillhub.scanengine.sunmi.SunmiScanner
import java.lang.ref.WeakReference

class ScanEngine private constructor(activity: ComponentActivity) {

    val scanner: Scanner by lazy {
        when (ScannerManufacturer.get()) {
            ScannerManufacturer.SUNMI -> SunmiScanner(WeakReference(activity))
            ScannerManufacturer.OTHER -> GoogleScanner(WeakReference(activity))
        }
    }

    companion object : SingletonHolder<ScanEngine, ComponentActivity>(::ScanEngine)
}
