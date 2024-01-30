import org.gradle.api.JavaVersion

object ConfigData {
    const val artifactId = "scan-engine"
    const val applicationId = "de.tillhub.scanengine"
    const val minSdkVersion = 21
    const val targetSdkVersion = 34
    const val versionName = "1.3.1"

    val JAVA_VERSION = JavaVersion.VERSION_17
    val JVM_TARGET = JAVA_VERSION.toString()
}
