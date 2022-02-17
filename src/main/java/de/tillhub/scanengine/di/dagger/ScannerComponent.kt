package de.tillhub.scanengine.di.dagger

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import de.tillhub.scanengine.Scanner

@ScannerScope
@Component(modules = [(ScannerDaggerModule::class)])
interface ScannerComponent {

    @Component.Builder
    interface Builder {
        fun context(@BindsInstance context: Context): Builder
        fun build(): ScannerComponent
    }

    fun scanner(): Scanner
}
