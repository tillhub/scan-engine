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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import de.tillhub.scanengine.CameraScanner
import de.tillhub.scanengine.ScanEngine
import de.tillhub.scanengine.data.ScanEvent
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
                                    scanEngine.barcodeScanner.disconnect(id)
                                }
                            }
                        ) {
                            Text(text = "Disconnect scanner")
                        }
                        Spacer(modifier = Modifier.height(36.dp))
                        ShowScannerList(scannerList)
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

    @Composable
    private fun ShowScannerList(scanners: List<Scanner>) {
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
                    Column {
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
                            .clickable {
                                selectedScannerId.value = inactiveScanner.id
                                connectScanner(coroutineScope, inactiveScanner.id, errorDrawable)
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

                ScannerResponse.Error.NotFound-> Toast.makeText(this@MainActivity, "Scanner Not found ", Toast.LENGTH_SHORT).show()
                ScannerResponse.Success -> Toast.makeText(this@MainActivity, "Scanner connected ", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

