package de.tillhub.scanengine.camera.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class CameraScanActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CameraScreen(
                onResult = { barcode ->
                    val resultIntent =
                        Intent().apply {
                            putExtra(DATA_KEY, barcode)
                            intent.getStringExtra(SCAN_KEY)?.let {
                                putExtra(SCAN_KEY, it)
                            }
                        }
                    setResult(RESULT_OK, resultIntent)
                    finish()
                },
                onDismiss = {
                    setResult(RESULT_CANCELED)
                    finish()
                },
            )
        }
    }

    companion object {
        const val DATA_KEY = "scanned_data"
        const val SCAN_KEY = "scan_key"
    }
}
