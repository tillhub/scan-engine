package de.tillhub.scanengine.sunmi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.common.safeLet
import de.tillhub.scanengine.helper.serializable

class SunmiScannerActivityContract : ActivityResultContract<Intent, List<ScanEvent>>() {

    override fun createIntent(context: Context, input: Intent) = input

    override fun parseResult(resultCode: Int, intent: Intent?): List<ScanEvent> =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            evaluateScanResult(it)
        } ?: listOf(ScanEvent.Canceled)

    private fun evaluateScanResult(extras: Bundle): List<ScanEvent> {
        val rawCodes: List<Map<String, String>> = extras.serializable(SunmiScanner.DATA) ?: emptyList()

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
            ScanEvent.Success(it.content)
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
