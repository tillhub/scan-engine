plugins {
    kotlin(Dependencies.KotlinPlugins.ANDROID)
    kotlin(Dependencies.KotlinPlugins.KAPT)
    id(Dependencies.Plugins.LIBRARY)
    id(Dependencies.Plugins.DETEKT) version Versions.Plugins.DETEKT
    id(Dependencies.Plugins.HILT)
    id("maven-publish")
}

android {
    compileSdk = ConfigData.targetSdkVersion

    defaultConfig {
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        getByName(ConfigData.BuildType.RELEASE) {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName(ConfigData.BuildType.DEBUG) {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = ConfigData.JAVA_VERSION
        targetCompatibility = ConfigData.JAVA_VERSION
    }

    hilt {
        enableExperimentalClasspathAggregation = true
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
}

dependencies {
    // Groups
    implementDependencyGroup(Dependencies.Groups.CORE)
    implementDependencyGroup(Dependencies.Groups.CORE_UI)
    implementDependencyGroup(Dependencies.Groups.CAMERA)

    // Firebase
    implementation(platform(Dependencies.Firebase.BOM))

    // Timber
    implementation(Dependencies.Tools.TIMBER)

    // ML Kit
    implementation(Dependencies.Google.MLKIT_BARCODES)

    // Module Specific dependencies
    implementation(Dependencies.AndroidX.CONSTRAINT_LAYOUT)

    // Unit tests
    implementDependencyGroup(Dependencies.Groups.TEST_LIBRARIES)

    // Android tests
    implementDependencyGroup(Dependencies.Groups.ANDROID_TEST_LIBRARIES)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = ConfigData.applicationId
            artifactId = ConfigData.artifactId
            version = ConfigData.versionName
        }
    }
}
