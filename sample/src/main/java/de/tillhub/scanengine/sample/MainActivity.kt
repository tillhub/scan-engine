package de.tillhub.scanengine.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.data.ScanEvent
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.sample.ui.theme.TillhubScanEngineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val scanEngine by lazy {
        ScanEngine.getInstance(applicationContext).initBarcodeScanners(ScannerType.ZEBRA)
    }
    private lateinit var cameraScanner: CameraScanner
    private var scanCode = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraScanner = scanEngine.newCameraScanner(this)

        setContent {
            TillhubScanEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = scanCode.value)
                        Spacer(modifier = Modifier.height(36.dp))
                        Button(
                            onClick = {
                                cameraScanner.startCameraScanner("key")
                            }
                        ) {
                            Text(text = "Start camera scanner")
                        }
                        Spacer(modifier = Modifier.height(36.dp))
                        Button(
                            onClick = {
                                scanEngine.barcodeScanner.startPairingScreen(ScannerType.ZEBRA)
                            }
                        ) {
                            Text(text = "Start zebra scanner")
                        }
                    }
                }
            }
        }
        scanEngine.barcodeScanner.scanWithKey("key")

        lifecycleScope.launch {
            scanEngine.observeScannerResults().collect {
                scanCode.value = (it as? ScanEvent.Success)?.value.orEmpty()
            }
        }
    }
}
