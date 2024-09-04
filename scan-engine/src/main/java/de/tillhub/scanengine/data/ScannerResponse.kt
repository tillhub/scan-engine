package de.tillhub.scanengine.data

import androidx.annotation.DrawableRes

sealed class ScannerResponse {
    sealed class Error : ScannerResponse() {
        data object NotFound : Error()
        class Connect(@DrawableRes val barcode: Int) : Error()
    }

    data object Success : ScannerResponse()
}
