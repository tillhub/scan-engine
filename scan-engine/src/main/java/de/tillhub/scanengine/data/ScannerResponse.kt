package de.tillhub.scanengine.data

import androidx.annotation.DrawableRes

sealed class ScannerResponse {
    sealed class Error {
        data object NotFound : ScannerResponse()
        class Connect(@DrawableRes val barcode: Int) : ScannerResponse()
    }

    data object Success : ScannerResponse()
}
