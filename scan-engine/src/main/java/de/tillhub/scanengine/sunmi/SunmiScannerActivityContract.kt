package de.tillhub.scanengine.sunmi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.ScannedDataResult
import de.tillhub.scanengine.common.safeLet

class SunmiScannerActivityContract : ActivityResultContract<Intent, List<ScannedDataResult>>() {

    override fun createIntent(context: Context, input: Intent) = input

    override fun parseResult(resultCode: Int, intent: Intent?): List<ScannedDataResult> =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            evaluateScanResult(it)
        } ?: listOf(ScannedDataResult.Canceled)
    private fun evaluateScanResult(extras: Bundle): List<ScannedDataResult> {
        @Suppress("UNCHECKED_CAST")
        val rawCodes = extras.getSerializable(SunmiScanner.DATA) as List<Map<String, String>>
        return rawCodes.mapNotNull {
            safeLet(
                it[SunmiScanner.RESPONSE_TYPE],
                it[SunmiScanner.RESPONSE_VALUE]
            ) { type, value ->
                ScanCode(
                    type.toScanCodeType(),
                    value
                )
            }
        }.map {
            ScannedDataResult.ScannedData(it.content)
        }
    }

    private fun String?.toScanCodeType() = when (this) {
        null -> SunmiScanner.ScanCodeType.Unknown
        else -> SunmiScanner.ScanCodeType.Type(this)
    }

    private data class ScanCode(
        val codeType: SunmiScanner.ScanCodeType,
        val content: String,
    )
}
