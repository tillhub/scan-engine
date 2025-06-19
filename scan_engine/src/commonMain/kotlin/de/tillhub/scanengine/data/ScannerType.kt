package de.tillhub.scanengine.data

import de.tillhub.scanengine.getPlatform

enum class ScannerType(val value: String) {
    SUNMI("SUNMI"),
    ZEBRA("ZEBRA"),
    UNKNOWN("UNKNOWN");

    companion object {
        fun get(): ScannerType =
            entries.firstOrNull {
                it.value == getPlatform().manufacturer
            } ?: UNKNOWN
    }
}
