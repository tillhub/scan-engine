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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.ScanEvent
import de.tillhub.scanengine.sample.ui.theme.TillhubScanEngineTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val scanner by lazy { ScanEngine.getInstance(this).scanner }
    private val scanCode = mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.CREATED) {
                scanner.observeScannerResults()
                    .collect {
                        scanCode.value = (it as? ScanEvent.Success)?.value.orEmpty()
                    }
            }
        }

        setContent {
            TillhubScanEngineTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Greeting(scanCode.value) {
                        scanner.startCameraScanner("key")
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(value: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value
        )
        Spacer(modifier = Modifier.height(36.dp))
        Button(
            onClick = onClick
        ) {
            Text(text = "Start camera scanner")
        }
    }
}
