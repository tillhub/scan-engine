package de.tillhub.scanengine.di.hilt

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.di.ScannerInjectionProvider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object HiltScannerModule {

    @Provides
    @Singleton
    fun provideScanner(
        @ApplicationContext appContext: Context,
    ): Scanner = ScannerInjectionProvider.provideScanner(appContext)
}
