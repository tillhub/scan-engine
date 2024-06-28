package de.tillhub.scanengine.data
sealed class ScanEvent {
    data object NotConnected : ScanEvent()
    data object Connected : ScanEvent()
    class InProgress(val scanKey: String?) : ScanEvent()
    class Success(
        val value: String,
        val scanKey: String? = null
    ) : ScanEvent() {
        fun copy(
            value: String = this.value,
            scanKey: String? = this.scanKey
        ) = Success(value, scanKey)
    }
    data object Canceled : ScanEvent()
}
