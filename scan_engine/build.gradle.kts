import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
}

kotlin {
    compilerOptions {
        // removes warnings for expect/actual classes
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)
    }

    val xcfName = "scan_engine"
    iosX64 { binaries.framework { baseName = xcfName } }
    iosArm64 { binaries.framework { baseName = xcfName } }
    iosSimulatorArm64 { binaries.framework { baseName = xcfName } }

    sourceSets {
        commonMain.dependencies {
            implementation(libs.bundles.core)

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.bundles.camera)
            implementation(libs.bundles.mlkit)
            implementation(libs.activity.compose)
        }
    }
}

android {
    namespace = Configs.APPLICATION_ID
    compileSdk = Configs.COMPILE_SDK

    experimentalProperties["android.experimental.kmp.enableAndroidResources"] = true

    defaultConfig {
        minSdk = Configs.MIN_SDK
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    compileOptions {
        sourceCompatibility = Configs.JAVA_VERSION
        targetCompatibility = Configs.JAVA_VERSION
    }
}

compose.resources {
    packageOfResClass = "de.tillhub.scanengine.resources"
    generateResClass = auto
}