package de.tillhub.scanengine.data

import androidx.annotation.DrawableRes

sealed class ScannerResponse {
    sealed class Error : ScannerResponse() {
        data object NotFound : Error()
        data object Disconnect : Error()
        class Connect(@DrawableRes val barcode: Int) : Error()
    }

    sealed class Success : ScannerResponse() {
        data object Connect : Success()
        data object Disconnect : Success()
    }
}
