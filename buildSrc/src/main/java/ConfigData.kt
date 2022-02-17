import org.gradle.api.JavaVersion

object ConfigData {
    const val artifactId = "scan-engine"
    const val applicationId = "de.tillhub.scanengine"
    const val minSdkVersion = 21
    const val targetSdkVersion = 31
    const val versionCode = 1
    const val versionName = "1.3.0"

    val JAVA_VERSION = JavaVersion.VERSION_11
    val JVM_TARGET = JavaVersion.VERSION_11.toString()
}
