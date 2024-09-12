package de.tillhub.scanengine.data
sealed class ScannerEvent {
    sealed class External : ScannerEvent() {
        data object NotConnected : External()
        data object Connected : External()
    }
    sealed class Camera : ScannerEvent() {
        class InProgress(val scanKey: String?) : Camera()
        data object Canceled : Camera()
    }
    class Success(
        val value: String,
        val scanKey: String? = null
    ) : ScannerEvent() {
        fun copy(
            value: String = this.value,
            scanKey: String? = this.scanKey
        ) = Success(value, scanKey)
    }
}
