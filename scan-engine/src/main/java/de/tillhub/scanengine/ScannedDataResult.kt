package de.tillhub.scanengine

sealed class ScannedDataResult {
    data class ScannedData(
        val value: String,
        val scanKey: String? = null
    ) : ScannedDataResult()

    data object Canceled : ScannedDataResult()
}

