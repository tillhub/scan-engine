package de.tillhub.scanengine.di.dagger

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.migration.DisableInstallInCheck
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.di.ScannerInjectionProvider

@Module
@DisableInstallInCheck
object ScannerDaggerModule {

    @Provides
    @ScannerScope
    fun provideScanner(
        appContext: Context,
    ): Scanner = ScannerInjectionProvider.provideScanner(appContext)
}
