package de.tillhub.scanengine

enum class ScannerManufacturer {
    SUNMI,
    OTHER;

    companion object {
        private const val SUNMI_MANUFACTURER_NAME = "SUNMI"
        fun get(): ScannerManufacturer =
            if (android.os.Build.MANUFACTURER.compareTo(SUNMI_MANUFACTURER_NAME) == 0) {
                SUNMI
            } else OTHER
    }
}
