
object Dependencies {

    object Plugins {
        const val LIBRARY = "com.android.library"
        const val DETEKT = "io.gitlab.arturbosch.detekt"
        const val HILT = "dagger.hilt.android.plugin"
        const val PUBLISH = "maven-publish"
    }

    object KotlinPlugins {
        const val ANDROID = "android"
        const val KAPT = "kapt"
    }

    object Tools {
        const val TIMBER = "com.jakewharton.timber:timber:${Versions.Tools.TIMBER}"
    }

    object Kotlin {
        const val COROUTINES = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.Kotlin.COROUTINES}"
        const val COROUTINES_TEST = "org.jetbrains.kotlinx:kotlinx-coroutines-test:${Versions.Kotlin.COROUTINES}"
    }

    object AndroidX {
        const val CORE_KTX = "androidx.core:core-ktx:${Versions.AndroidX.CORE_KTX}"
        const val APP_COMPAT = "androidx.appcompat:appcompat:${Versions.AndroidX.APP_COMPAT}"

        const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout:${Versions.AndroidX.CONSTRAINT_LAYOUT}"

        const val FRAGMENT_TESTING = "androidx.fragment:fragment-testing:${Versions.AndroidX.FRAGMENT}"
    }

    object Google {
        const val MATERIAL = "com.google.android.material:material:${Versions.Google.MATERIAL}"
        const val HILT = "com.google.dagger:hilt-android:${Versions.Google.HILT}"
        const val HILT_COMPILER = "com.google.dagger:hilt-android-compiler:${Versions.Google.HILT}"
        const val HILT_TESTING = "com.google.dagger:hilt-android-testing:${Versions.Google.HILT}"
        const val MLKIT_BARCODES = "com.google.mlkit:barcode-scanning:${Versions.Google.MLKIT}"
    }

    object CameraX {
        const val CAMERA2 = "androidx.camera:camera-camera2:${Versions.CameraX.CAMERA_X}"
        const val CAMERA_LIFECYCLE = "androidx.camera:camera-lifecycle:${Versions.CameraX.CAMERA_X}"
        const val CAMERA_VIEW = "androidx.camera:camera-view:${Versions.CameraX.CAMERA_VIEW}"
    }

    object Testing {
        const val JUNIT = "junit:junit:${Versions.Testing.JUNIT}"
        const val MOCKK = "io.mockk:mockk:${Versions.Testing.MOCKK}"
        const val MOCKK_AGENT_JVM = "io.mockk:mockk-agent-jvm:${Versions.Testing.MOCKK}"
        const val ROBOLECTRIC = "org.robolectric:robolectric:${Versions.Testing.ROBOLECTRIC}"
        const val KOTEST_RUNNER = "io.kotest:kotest-runner-junit5:${Versions.Testing.KOTEST}"
        const val KOTEST_ASSERTIONS = "io.kotest:kotest-assertions-core:${Versions.Testing.KOTEST}"
        const val KOTEST_PROPERTY = "io.kotest:kotest-property:${Versions.Testing.KOTEST}"
        const val KOTEST_ROBOLECTRIC = "io.kotest.extensions:kotest-extensions-robolectric:${Versions.Testing.KOTEST_ROBOLECTRIC}"
    }

    object AndroidTesting {
        // Core library
        const val CORE = "androidx.test:core-ktx:${Versions.AndroidTest.CORE}"

        // AndroidJUnitRunner and JUnit Rules
        const val RUNNER = "androidx.test:runner:${Versions.AndroidTest.RUNNER}"
        const val RULES = "androidx.test:rules:${Versions.AndroidTest.RULES}"

        // Assertions
        const val JUNIT = "androidx.test.ext:junit:${Versions.AndroidTest.JUNIT}"
        const val TRUTH = "androidx.test.ext:truth:${Versions.AndroidTest.TRUTH}"

        const val ESPRESSO_CORE = "androidx.test.espresso:espresso-core:${Versions.AndroidTest.ESPRESSO_CORE}"
        const val MOCKK = "io.mockk:mockk-android:${Versions.Testing.MOCKK}"
    }

    object Groups {
        val CORE = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(AndroidX.CORE_KTX))
            add(Dependency.Implementation(Google.HILT))
            add(Dependency.Kapt(Google.HILT_COMPILER))
            add(Dependency.Implementation(Kotlin.COROUTINES))
        }

        val CORE_UI = arrayListOf<Dependency>().apply {
            add(Dependency.Implementation(AndroidX.APP_COMPAT))
            add(Dependency.Implementation(Google.MATERIAL))
        }

        val TEST_LIBRARIES = arrayListOf<Dependency>().apply {
            add(Dependency.TestImplementation(Testing.JUNIT))
            add(Dependency.TestImplementation(Testing.MOCKK))
            add(Dependency.TestImplementation(Testing.MOCKK_AGENT_JVM))
            add(Dependency.TestImplementation(Testing.KOTEST_RUNNER))
            add(Dependency.TestImplementation(Testing.KOTEST_ASSERTIONS))
            add(Dependency.TestImplementation(Testing.KOTEST_PROPERTY))
            add(Dependency.TestImplementation(Kotlin.COROUTINES_TEST))
        }

        val TEST_ROBOLECTRIC = arrayListOf<Dependency>().apply {
            add(Dependency.TestImplementation(AndroidTesting.CORE))
            add(Dependency.TestImplementation(Google.HILT_TESTING))
            add(Dependency.KaptTest(Google.HILT_COMPILER))
            add(Dependency.TestImplementation(Testing.ROBOLECTRIC))
            add(Dependency.TestImplementation(Testing.KOTEST_ROBOLECTRIC))
        }

        val ANDROID_TEST_LIBRARIES = arrayListOf<Dependency>().apply {
            add(Dependency.DebugImplementation(AndroidX.FRAGMENT_TESTING))
            add(Dependency.AndroidTestImplementation(AndroidTesting.CORE))
            add(Dependency.AndroidTestImplementation(AndroidTesting.RUNNER))
            add(Dependency.AndroidTestImplementation(AndroidTesting.RULES))
            add(Dependency.AndroidTestImplementation(AndroidTesting.JUNIT))
            add(Dependency.AndroidTestImplementation(AndroidTesting.TRUTH))
            add(Dependency.AndroidTestImplementation(AndroidTesting.ESPRESSO_CORE))
            add(Dependency.AndroidTestImplementation(AndroidTesting.MOCKK))
            add(Dependency.AndroidTestImplementation(Google.HILT_TESTING))
            add(Dependency.KaptAndroidTest(Google.HILT_COMPILER))
        }
    }
}
