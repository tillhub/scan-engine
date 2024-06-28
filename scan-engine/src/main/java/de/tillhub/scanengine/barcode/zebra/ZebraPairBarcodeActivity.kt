package de.tillhub.scanengine.barcode.zebra

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.IDcsSdkApi
import de.tillhub.scanengine.R
import de.tillhub.scanengine.barcode.zebra.ZebraBarcodeScanner.Companion.BLUETOOTH_PERMISSIONS

internal class ZebraPairBarcodeActivity : ComponentActivity() {

    private val viewModel: ZebraPairBarcodeViewModel by viewModels {
        ZebraPairBarcodeViewModel.factory(context = applicationContext)
    }
    private var loading by mutableStateOf(true)
    private var showPermissionDialog by mutableStateOf(false)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.initScanner()
            loading = false
            showPermissionDialog = false
        } else {
            showPermissionDialog = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZebraPairBarcodeActivityContent()
        }
    }

    @Composable
    private fun ZebraPairBarcodeActivityContent() {
        val isConnected by viewModel.isConnected.collectAsState()

        LaunchedEffect(Unit) {
            requestBluetoothPermission()
        }

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (loading) {
                Text(stringResource(R.string.loading))
            } else {
                PairingDialog(sdkHandler = viewModel.getSdkHandler()) {
                    finish()
                }
            }
        }

        if (isConnected) {
            this@ZebraPairBarcodeActivity.finish()
        }
        if (showPermissionDialog) {
            PermissionAlertDialog(
                title = R.string.permission_required,
                message = R.string.permissions_required_desc,
                confirmText = R.string.settings,
                onConfirm = {
                    openAppSettings()
                    showPermissionDialog = false
                }
            )
        }
    }

    private fun requestBluetoothPermission() {
        val neededPermissions =
            BLUETOOTH_PERMISSIONS.filter {
                ContextCompat.checkSelfPermission(
                    this,
                    it
                ) != PackageManager.PERMISSION_GRANTED
            }
        if (neededPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(neededPermissions.toTypedArray())
        } else {
            viewModel.initScanner()
            loading = false
        }
    }

    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts(SCHEME, packageName, null)
        }
        startActivity(intent)
    }

    companion object {
        private const val SCHEME = "package"
    }
}

@Composable
private fun PermissionAlertDialog(
    @StringRes title: Int,
    @StringRes message: Int,
    @StringRes confirmText: Int,
    onConfirm: () -> Unit,
    dismissOnBackPress: Boolean = false,
    dismissOnClickOutside: Boolean = false
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(title)) },
        text = { Text(stringResource(message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(confirmText))
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside
        )
    )
}

@Composable
private fun PairingDialog(sdkHandler: IDcsSdkApi, hideDialog: () -> Unit) {
    Column {
        AndroidView(
            factory = {
                sdkHandler.dcssdkGetPairingBarcode(
                    DCSSDKDefs.DCSSDK_BT_PROTOCOL.SSI_BT_LE,
                    DCSSDKDefs.DCSSDK_BT_SCANNER_CONFIG.SET_FACTORY_DEFAULTS
                )
            }
        )
        Button(modifier = Modifier.fillMaxWidth(), onClick = hideDialog) {
            Text(stringResource(R.string.cancel))
        }
    }
}
