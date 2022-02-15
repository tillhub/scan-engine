package de.tillhub.scanengine.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.tillhub.scanengine.Scanner
import de.tillhub.scanengine.ScannerManufacturer
import de.tillhub.scanengine.common.CoroutineScopeProvider
import de.tillhub.scanengine.common.CoroutineScopeProviderImpl
import de.tillhub.scanengine.google.GoogleScanner
import de.tillhub.scanengine.sunmi.SunmiScanner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EngineModule {

    @Provides
    @Singleton
    fun provideCoroutineScopeProvider(): CoroutineScopeProvider = CoroutineScopeProviderImpl()

    @Provides
    @Singleton
    fun provideScanner(
        @ApplicationContext appContext: Context,
        coroutineScopeProvider: CoroutineScopeProvider,
    ): Scanner = when (ScannerManufacturer.get()) {
        ScannerManufacturer.SUNMI -> SunmiScanner(coroutineScopeProvider)
        ScannerManufacturer.OTHER -> GoogleScanner(appContext)
    }
}
