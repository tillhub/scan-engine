package de.tillhub.scanengine.data
sealed class ScanEvent {
    class Success(
        val value: String,
        val scanKey: String? = null
    ) : ScanEvent() {
        fun copy(
            value: String = this.value,
            scanKey: String? = this.scanKey
        ) = Success(value, scanKey)
    }
    class InProgress(val scanKey: String?) : ScanEvent()

    data object Canceled : ScanEvent()

    data object Idle : ScanEvent()
}
