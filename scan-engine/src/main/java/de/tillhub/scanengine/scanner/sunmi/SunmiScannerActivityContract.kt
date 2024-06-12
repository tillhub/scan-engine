package de.tillhub.scanengine.scanner.sunmi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.os.BundleCompat
import de.tillhub.scanengine.common.safeLet
import de.tillhub.scanengine.data.ScanEvent

internal class SunmiScannerActivityContract : ActivityResultContract<Unit, List<ScanEvent>>() {

    override fun createIntent(context: Context, input: Unit) = Intent("com.summi.scan").apply {
        setPackage("com.sunmi.sunmiqrcodescanner")

        // Additional intent options:
        //
        // The current preview resolution ,PPI_1920_1080 = 0X0001;PPI_1280_720 = 0X0002;PPI_BEST = 0X0003;
        // putExtra("CURRENT_PPI", 0X0003)
        //
        // Whether to identify inverse code
        // putExtra("IDENTIFY_INVERSE_QR_CODE", true)
        //
        // Vibrate after scanning, default false, only support M1 right now.
        // putExtra("PLAY_VIBRATE", false)

        // Prompt tone after scanning (default true)
        putExtra("PLAY_SOUND", true)

        // Whether to display the settings button at the top-right corner (default true)
        putExtra("IS_SHOW_SETTING", true)

        // Whether to display the album button (default true)
        putExtra("IS_SHOW_ALBUM", false)

        // Whether to identify several codes at once (default false)
        putExtra("IDENTIFY_MORE_CODE", false)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<ScanEvent> =
        intent.takeIf { resultCode == Activity.RESULT_OK }?.extras?.let {
            evaluateScanResult(it)
        } ?: listOf(ScanEvent.Canceled)

    private fun evaluateScanResult(extras: Bundle): List<ScanEvent> {
        val rawCodes: List<Map<String, String>> =
            BundleCompat.getParcelableArrayList(
                extras,
                SunmiCameraScanner.DATA,
                mapOf<String, String>()::class.java
            ) ?: emptyList()

        return rawCodes.mapNotNull {
            safeLet(
                it[SunmiCameraScanner.RESPONSE_TYPE],
                it[SunmiCameraScanner.RESPONSE_VALUE]
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
        null -> SunmiCameraScanner.ScanCodeType.Unknown
        else -> SunmiCameraScanner.ScanCodeType.Type(this)
    }

    private data class ScanCode(
        val codeType: SunmiCameraScanner.ScanCodeType,
        val content: String,
    )
}
