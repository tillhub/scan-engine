import org.gradle.api.artifacts.dsl.DependencyHandler

// Util functions for easier adding different type of dependencies

sealed class Dependency {
    abstract val configurationName: String
    abstract val dependencyNotation: String

    data class Implementation(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = IMPLEMENTATION
    }
    data class DebugImplementation(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = DEBUG_IMPLEMENTATION
    }
    data class Kapt(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = KAPT
    }
    data class KaptTest(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = KAPT_TEST
    }
    data class KaptAndroidTest(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = KAPT_ANDROID_TEST
    }
    data class TestImplementation(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = TEST_IMPLEMENTATION
    }
    data class AndroidTestImplementation(override val dependencyNotation: String): Dependency() {
        override val configurationName: String
            get() = ANDROID_TEST_IMPLEMENTATION
    }

    companion object {
        private const val KAPT = "kapt"
        private const val KAPT_TEST = "kaptTest"
        private const val KAPT_ANDROID_TEST = "kaptAndroidTest"
        private const val IMPLEMENTATION = "implementation"
        private const val DEBUG_IMPLEMENTATION = "implementation"
        private const val TEST_IMPLEMENTATION = "testImplementation"
        private const val ANDROID_TEST_IMPLEMENTATION = "androidTestImplementation"
    }
}


fun DependencyHandler.implementDependencyGroup(list: List<Dependency>) {
    list.forEach { dependency ->
        add(dependency.configurationName, dependency.dependencyNotation)
    }
}
