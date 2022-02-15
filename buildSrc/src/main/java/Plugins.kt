
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.kotlin.dsl.withType

fun Project.configureLibraryAndroidBlock() {
    extensions.getByType<LibraryExtension>().run {
        compileSdk = ConfigData.targetSdkVersion

        defaultConfig {
            minSdk = ConfigData.minSdkVersion
            targetSdk = ConfigData.targetSdkVersion
            consumerProguardFiles("consumer-rules.pro")
        }

        compileOptions {
            sourceCompatibility = ConfigData.JAVA_VERSION
            targetCompatibility = ConfigData.JAVA_VERSION
        }

        buildFeatures {
            viewBinding = true
        }

        // Kotest on Android uses the JUnit Platform gradle plugin.
        tasks.withType<Test> {
            useJUnitPlatform()
        }

        tasks.withType<KotlinCompile> {
            kotlinOptions {
                jvmTarget = ConfigData.JVM_TARGET
            }
        }
    }
}

fun Project.configureVersionCode(versionCode: Int, versionName: String) {
    extensions.getByType<BaseExtension>().run {
        defaultConfig {
            this.versionCode = versionCode
            this.versionName = versionName
        }
    }
}
