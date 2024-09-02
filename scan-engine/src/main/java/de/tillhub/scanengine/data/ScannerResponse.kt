package de.tillhub.scanengine.data

import androidx.annotation.DrawableRes

sealed class ScannerResponse {
    data object NotFound : ScannerResponse()
    data class Error(@DrawableRes val barcode: Int) : ScannerResponse()
    data object Success : ScannerResponse()
}
