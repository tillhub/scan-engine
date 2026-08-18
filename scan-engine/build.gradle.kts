import dev.mokkery.gradle.ApplicationRule
import org.jetbrains.compose.ExperimentalComposeLibrary
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSetTree

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.maven.publish)
}

kotlin {
    compilerOptions {
        // removes warnings for expect/actual classes
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        instrumentedTestVariant.sourceSetTree.set(KotlinSourceSetTree.test)

        publishLibraryVariants("release")

        dependencies {
            androidTestImplementation(libs.androidx.ui.test.junit4.android)
            androidTestImplementation(libs.androidx.ui.test.manifest)

            testImplementation(libs.robolectric)
            testImplementation(libs.androidx.test.core)
        }
    }

    val xcfName = "scan-engine"
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
        androidMain.dependencies {
            implementation(libs.bundles.camera)
            implementation(libs.bundles.mlkit)
            implementation(libs.activity.compose)
        }

        commonTest.dependencies {
            implementation(kotlin("test"))

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
        iosTest.dependencies {
            implementation(kotlin("test"))

            @OptIn(ExperimentalComposeLibrary::class)
            implementation(compose.uiTest)
        }
    }
}

mokkery {
    rule.set(ApplicationRule.AllTests)
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

    testOptions {
        unitTests {
            all {
                // We want to exclude all UI tests from the unit tests
                it.exclude(
                    "**/scanengine/ui/components/**",
                    "**/scanengine/camera/ui/**",
                )
            }
        }
    }
}

compose.resources {
    packageOfResClass = "de.tillhub.scanengine.resources"
    generateResClass = auto
}

mavenPublishing {
    // Define coordinates for the published artifact
    coordinates(
        groupId = "io.github.tillhub",
        artifactId = "scan-engine",
        version =
            libs.versions.scan.engine
                .get(),
    )

    // Configure POM metadata for the published artifact
    pom {
        name.set("Scan Engine")
        description.set("Kotlin MultiPlatform Library that provides unified barcode scanning functionality for Android and iOS platforms")
        inceptionYear.set("2025")
        url.set("https://github.com/tillhub/scan-engine")

        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }

        // Specify developers information
        developers {
            developer {
                id.set("djordjeh")
                name.set("Đorđe Hrnjez")
                email.set("dorde.hrnjez@unzer.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/tillhub/scan-engine")
        }
    }

    // Configure publishing to Maven Central
    publishToMavenCentral()

    // Enable GPG signing for all publications
    signAllPublications()
}
