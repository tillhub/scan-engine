package de.tillhub.scanengine.sample

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.camera.ui.CameraScreen
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.sample.ui.theme.Tillhub_Scan_EngineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val scanEngine by lazy {
        ScanEngine.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Tillhub_Scan_EngineTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    scanEngine.getCameraView(
                        scanKey = "camera_scan_key",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        lifecycleScope.launch {
            scanEngine.observeScannerResults().collect {
                (it as? ScannerEvent.ScanResult)?.let { result ->
                    Log.d("SCANNER", result.scanKey + " " + result.value)
                }
            }
        }
    }
}