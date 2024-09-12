package de.tillhub.scanengine.data

enum class ScannerType(val value: String) {
    SUNMI("SUNMI"),
    ZEBRA("ZEBRA"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun get(): ScannerType =
            entries.firstOrNull {
                it.value == android.os.Build.MANUFACTURER
            } ?: UNKNOWN
    }
}
