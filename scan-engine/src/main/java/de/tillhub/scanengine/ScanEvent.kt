package de.tillhub.scanengine

sealed class ScanEvent {
    data class Success(
        val value: String,
        val scanKey: String? = null
    ) : ScanEvent()

    data object InProgress : ScanEvent()

    data object Canceled : ScanEvent()
}
