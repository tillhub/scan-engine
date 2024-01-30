plugins {
    id("kotlin-android")
    id("com.android.library")
    id("maven-publish")
    id("io.gitlab.arturbosch.detekt")
}

android {
    compileSdk = ConfigData.targetSdkVersion
    defaultConfig {
        minSdk = ConfigData.minSdkVersion
    }

    buildFeatures {
        viewBinding = true
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

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }

    namespace = "de.tillhub.scanengine"
}

dependencies {
    // Groups
    implementation(libs.bundles.core)
    implementation(libs.bundles.ui)
    implementation(libs.bundles.lifecycle)

    // Utils
    coreLibraryDesugaring(libs.android.desugarJdkLibs)
    implementation(libs.timber)
    detektPlugins(libs.detekt.formatting)

    // Module Specific dependencies
    implementation(libs.androidx.constraintlayout)

    // ML Kit
    api(libs.bundles.mlkit)

    // Camera
    api(libs.bundles.camera)

    // Unit tests
    testImplementation(libs.bundles.testing)
    // Android tests
    androidTestImplementation(libs.bundles.core)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>(ConfigData.artifactId) {
                groupId = ConfigData.applicationId
                artifactId = ConfigData.artifactId
                version = ConfigData.versionName

                from(components.getByName("release"))
            }
        }
    }
}
