plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.detekt)
    id("maven-publish")
}

android {
    namespace = "de.tillhub.scanengine"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = JavaVersion.VERSION_17.toString()
        }
    }
}

detekt {
    buildUponDefaultConfig = true // preconfigure defaults
    allRules = false // activate all available (even unstable) rules.
    config.setFrom("$projectDir/config/detekt.yml")
}

dependencies {

    implementation(libs.androidx.core)
    implementation(libs.androidx.appcompat)
    implementation(libs.google.material)

    // Groups
    implementation(libs.bundles.core)
    implementation(libs.bundles.ui)
    implementation(libs.bundles.lifecycle)

    // Utils
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(libs.timber)
    detektPlugins(libs.detekt.formatting)
    detektPlugins(libs.detekt.libraries)

    // Module Specific dependencies
    implementation(libs.androidx.constraintlayout)

    // ML Kit
    implementation(libs.bundles.mlkit)

    // Camera
    implementation(libs.bundles.camera)

    // Unit tests
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.robolectric)
    androidTestImplementation(libs.bundles.testing.android)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("scan-engine") {
                groupId = "de.tillhub.scanengine"
                artifactId = "scan-engine"
                version = "1.4.7"

                from(components.getByName("release"))
            }
        }
    }
}
