package de.tillhub.scanengine.common

internal enum class Manufacturer(open val value: String) {
    VERIFONE("Verifone"),
    OTHER("OTHER");

    companion object {
        fun get(): Manufacturer =
            entries.firstOrNull {
                it.value == Build.MANUFACTURER
            } ?: OTHER
        fun matches(value: Manufacturer): Boolean = get() == value
    }
}
