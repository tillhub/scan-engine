plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("io.gitlab.arturbosch.detekt")
}

android {
    namespace = ConfigData.applicationId
    compileSdk = ConfigData.targetSdkVersion

    defaultConfig {
        minSdk = ConfigData.minSdkVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildFeatures {
        viewBinding = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

    // Module Specific dependencies
    implementation(libs.androidx.constraintlayout)

    // ML Kit
    implementation(libs.bundles.mlkit)

    // Camera
    implementation(libs.bundles.camera)

    // Unit tests
    testImplementation(libs.bundles.testing)
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
