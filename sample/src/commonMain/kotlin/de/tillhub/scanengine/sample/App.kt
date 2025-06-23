package de.tillhub.scanengine.sample

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.sample.theme.ScanEngineTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {
    var currentScreen by remember { mutableStateOf<ScanScreen?>(null) }

    val scanEngine by lazy { ScanEngine.getInstance() }
    val scannedResult = remember { mutableStateOf("No result") }

    val cameraScannerLauncher = scanEngine.newCameraScanner().cameraScannerLauncher()

    CoroutineScope(Job() + Dispatchers.Main).launch {
        scanEngine.observeScannerResults().collect {
            (it as? ScannerEvent.ScanResult)?.let { result ->
                scannedResult.value = result.value
            }
        }
    }

    ScanEngineTheme {
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(WindowInsets.systemBars.asPaddingValues())
                .padding(16.dp)
        ) {
            Column {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp)
                            .wrapContentWidth(),
                        text = "Results : ${scannedResult.value}",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                ScanSection("Camera scan") {
                    currentScreen = ScanScreen.CameraScan
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            when (currentScreen) {
                ScanScreen.CameraScan -> LaunchedEffect(Unit) {
                    cameraScannerLauncher.launchCameraScanner()
                    currentScreen = null
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ScanSection(
    label: String,
    onClick: () -> Unit
) {
    Column {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClick
        ) {
            Text(text = label)
        }
    }
}

enum class ScanScreen {
    CameraScan, Bluetooth
}