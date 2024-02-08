allprojects {
    repositories {
        google()
        maven("https://jitpack.io")
        mavenCentral()
    }
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.gradlePlugin.kotlin)
    }
}

