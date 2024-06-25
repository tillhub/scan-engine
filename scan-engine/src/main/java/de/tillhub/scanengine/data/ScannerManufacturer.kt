package de.tillhub.scanengine.data

enum class ScannerManufacturer(val value: String) {
    SUNMI("SUNMI"),
    NOT_SUNMI("OTHER");

    companion object {
        fun get(): ScannerManufacturer =
            entries.firstOrNull {
                it.value == android.os.Build.MANUFACTURER
            } ?: NOT_SUNMI
    }
}
