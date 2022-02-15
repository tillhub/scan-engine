package de.tillhub.scanengine

/**
 * Represents a scan result which can be either a valid scan or an error.
 */
sealed class ScanEvent {
    class Success(val content: ScannedData) : ScanEvent() {
        override fun toString(): String = "ScanEvent.Success(content='$content')"
    }

    class Error(val error: Exception? = null) : ScanEvent() {
        override fun toString(): String = "ScanEvent.Error(error=$error)"
    }
}
