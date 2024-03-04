package de.tillhub.scanengine

sealed class ScanEvent {
    data class Success(
        val value: String,
        val scanKey: String? = null
    ) : ScanEvent()

    data class InProgress(val scanKey: String?) : ScanEvent()

    data object Canceled : ScanEvent()

    data object Idle : ScanEvent()
}
