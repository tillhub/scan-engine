plugins {
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.kotlinMultiplatform).apply(false)
    alias(libs.plugins.compose.compiler).apply(false)
    alias(libs.plugins.compose).apply(false)
    alias(libs.plugins.atomicfu).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlin.android).apply(false)
    alias(libs.plugins.spotless).apply(false)
}

subprojects {
    apply(plugin = "com.diffplug.spotless")
    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("**/*.kt")
            targetExclude("${layout.buildDirectory}/**/*.kt")

            ktlint()
        }

        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }
    }
}