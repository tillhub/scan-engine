package de.tillhub.scanengine.sample

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.data.ScannerEvent
import de.tillhub.scanengine.data.Scanner
import de.tillhub.scanengine.data.ScannerResponse
import de.tillhub.scanengine.data.ScannerType
import de.tillhub.scanengine.sample.ui.theme.TillhubScanEngineTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val scanEngine by lazy {
        ScanEngine.getInstance(applicationContext).initBarcodeScanners(ScannerType.ZEBRA)
    }
    private lateinit var cameraScanner: CameraScanner
    private var scanCode = mutableStateOf("")
    private val scannerList = mutableStateListOf<Scanner>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraScanner = scanEngine.newCameraScanner(this)
        lifecycleScope.launch {
            scanEngine.barcodeScanner.observeScanners().collect { scanners ->
                scannerList.clear()
                scannerList.addAll(scanners)
            }
        }
        setContent {
            TillhubScanEngineTheme {
                Content()
            }
        }
        scanEngine.barcodeScanner.scanWithKey("key")

        lifecycleScope.launch {
            scanEngine.observeScannerResults().collect {
                scanCode.value = (it as? ScannerEvent.ScanResult)?.value.orEmpty()
            }
        }
    }

    @Composable
    private fun Content() {
        var autoReconnect by remember { mutableStateOf(false) }
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Top,
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
                Spacer(modifier = Modifier.height(36.dp))
                Button(
                    onClick = {
                        scannerList.firstOrNull { it.isConnected }?.apply {
                            lifecycleScope.launch {
                                scanEngine.barcodeScanner.disconnect(id)
                            }
                        }
                    }
                ) {
                    Text(text = "Disconnect scanner")
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Auto-Reconnect: ")
                    Switch(
                        checked = autoReconnect,
                        onCheckedChange = {
                            Toast
                                .makeText(
                                    this@MainActivity,
                                    "Not implemented yet",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                            autoReconnect = it
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                ShowScannerList(scannerList, autoReconnect)
            }
        }
    }

    @Composable
    private fun ShowScannerList(scanners: List<Scanner>, autoReconnect: Boolean) {
        val coroutineScope = rememberCoroutineScope()
        val activeScanners = scanners.filter { it.isConnected }
        val inactiveScanners = scanners.filter { !it.isConnected }
        val errorDrawable = remember { mutableIntStateOf(0) }
        val selectedScannerId = remember { mutableStateOf("") }
        errorDrawable.intValue.takeIf { it != 0 }?.let { drawable ->
            Image(
                painter = painterResource(id = drawable),
                contentDescription = "Error",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
            )
        }
        LazyColumn {
            if (activeScanners.isNotEmpty()) {
                errorDrawable.intValue = 0
                item {
                    Text(
                        "Active Scanners", style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                    )
                }
                items(activeScanners) { activeScanner ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                lifecycleScope.launch {
                                    scanEngine.barcodeScanner.disconnect(activeScanner.id)
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            activeScanner.name,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            activeScanner.serialNumber,
                            modifier = Modifier.padding(bottom = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                }
            }
            if (inactiveScanners.isNotEmpty()) {
                item {
                    Text(
                        text = "Inactive Scanners",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(8.dp)
                    )
                }
                items(inactiveScanners) { inactiveScanner ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (!autoReconnect) {
                                    selectedScannerId.value = inactiveScanner.id
                                    connectScanner(coroutineScope, inactiveScanner.id, errorDrawable)
                                } else {
                                    Toast
                                        .makeText(
                                            this@MainActivity,
                                            getString(R.string.auto_reconnect_turnoff), Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                            .padding(8.dp)
                    ) {
                        Text(
                            inactiveScanner.name,
                            modifier = Modifier.padding(top = 8.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            inactiveScanner.serialNumber,
                            modifier = Modifier.padding(bottom = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }
    }

    private fun connectScanner(
        coroutineScope: CoroutineScope,
        inactiveScannerId: String,
        errorDrawable: MutableIntState
    ) {
        coroutineScope.launch {
            when (val res = scanEngine.barcodeScanner.connect(inactiveScannerId)) {
                is ScannerResponse.Error.Connect -> {
                    withContext(Dispatchers.Main) {
                        errorDrawable.intValue = res.barcode
                    }
                }

                ScannerResponse.Error.NotFound -> Toast.makeText(
                    this@MainActivity,
                    getString(R.string.scanner_not_found),
                    Toast.LENGTH_SHORT
                ).show()

                ScannerResponse.Error.Disconnect -> Toast.makeText(
                    this@MainActivity,
                    getString(R.string.scanner_disconnection_error),
                    Toast.LENGTH_SHORT
                ).show()

                ScannerResponse.Success.Connect -> Toast.makeText(
                    this@MainActivity,
                    getString(R.string.scanner_connected), Toast.LENGTH_SHORT
                )
                    .show()

                ScannerResponse.Success.Disconnect -> Toast.makeText(
                    this@MainActivity,
                    getString(R.string.scanner_disconnected), Toast.LENGTH_SHORT
                )
                    .show()
            }
        }
    }
}

