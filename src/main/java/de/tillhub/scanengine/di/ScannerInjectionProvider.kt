package de.tillhub.scanengine.di

import android.content.Context
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.ScannerManufacturer
import de.tillhub.scanengine.google.GoogleScanner
import de.tillhub.scanengine.sunmi.SunmiScanner

object ScannerInjectionProvider {

    fun provideScanner(
        appContext: Context,
    ): Scanner = when (ScannerManufacturer.get()) {
        ScannerManufacturer.SUNMI -> SunmiScanner()
        ScannerManufacturer.OTHER -> GoogleScanner(appContext)
    }
}
