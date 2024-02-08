plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "de.tillhub.scanengine.sample"

    compileSdk = ConfigData.targetSdkVersion

    defaultConfig {
        applicationId = "de.tillhub.scanengine.sample"
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    compileOptions {
        sourceCompatibility = ConfigData.JAVA_VERSION
        targetCompatibility = ConfigData.JAVA_VERSION
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = ConfigData.JVM_TARGET
        }
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.9"
    }
}

dependencies {

    implementation(project(":scan-engine"))

    implementation(libs.androidx.core)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
}
