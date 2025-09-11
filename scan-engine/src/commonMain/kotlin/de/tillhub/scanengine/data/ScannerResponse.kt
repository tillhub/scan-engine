package de.tillhub.scanengine.data

sealed class ScannerResponse {
    sealed class Error : ScannerResponse() {
        data object NotFound : Error()

        data object Disconnect : Error()

        class Connect(
            val barcode: Int,
        ) : Error() // TODO [barcode] should be
    }

    sealed class Success : ScannerResponse() {
        data object Connect : Success()

        data object Disconnect : Success()
    }
}
