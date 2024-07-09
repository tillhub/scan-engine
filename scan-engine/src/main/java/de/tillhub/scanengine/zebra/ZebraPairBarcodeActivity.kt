package de.tillhub.scanengine.zebra

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.zebra.scannercontrol.DCSSDKDefs
import com.zebra.scannercontrol.IDcsSdkApi
import de.tillhub.scanengine.R
import de.tillhub.scanengine.zebra.ZebraBarcodeScanner.Companion.BLUETOOTH_PERMISSIONS

internal class ZebraPairBarcodeActivity : ComponentActivity() {

    private val viewModel: ZebraPairBarcodeViewModel by viewModels {
        ZebraPairBarcodeViewModel.factory(context = applicationContext)
    }
    private val bluetoothManager: BluetoothManager by lazy {
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    }

    private val dialogEvent: MutableState<DialogEvent> by lazy {
        mutableStateOf(DialogEvent.Idle)
    }

    private val requestPermissionLauncher = registerForActivityResult(RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            if (bluetoothManager.adapter.isEnabled) {
                viewModel.initScanner()
            } else {
                openBluetoothSettings()
            }
        } else {
            grantPermissions(permissions, true)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ZebraPairBarcodeActivityContent()
        }
        checkBluetoothAccessibility()
    }

    @Composable
    private fun ZebraPairBarcodeActivityContent() {
        val state by viewModel.uiStateFlow.collectAsState()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (state) {
                ZebraPairBarcodeViewModel.State.Connected -> finish()
                ZebraPairBarcodeViewModel.State.Loading -> {
                    Text(stringResource(R.string.loading))
                }
                ZebraPairBarcodeViewModel.State.Pairing -> {
                    PairingDialog(sdkHandler = viewModel.getSdkHandler()) {
                        finish()
                    }
                }
            }
        }

        when (val event = dialogEvent.value) {
            DialogEvent.Idle -> Unit
            DialogEvent.FeatureError -> {
                ZebraAlertDialog(event) {
                    finish()
                }
            }
            DialogEvent.GrantPermissions -> {
                ZebraAlertDialog(event) {
                    dialogEvent.value = DialogEvent.Idle
                    requestPermissionLauncher.launch(getPermissionsState().keys.toTypedArray())
                }
            }
            DialogEvent.GrantPermissionsManually -> {
                ZebraAlertDialog(event) {
                    dialogEvent.value = DialogEvent.Idle
                    openAppSettings()
                }
            }
        }
    }

    private fun checkBluetoothAccessibility() {
        if (applicationContext.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            val permissions = getPermissionsState()
            if (permissions.all { it.value }) {
                if (bluetoothManager.adapter.isEnabled) {
                    viewModel.initScanner()
                } else {
                    openBluetoothSettings()
                }
            } else {
                grantPermissions(permissions)
            }
        } else {
            dialogEvent.value = DialogEvent.FeatureError
        }
    }

    private fun getPermissionsState() = BLUETOOTH_PERMISSIONS.associateWith {
        ContextCompat.checkSelfPermission(applicationContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun grantPermissions(permissions: Map<String, Boolean>, alreadyDeclined: Boolean = false) {
        when {
            permissions.any { shouldShowRequestPermissionRationale(it.key) } -> {
                dialogEvent.value = DialogEvent.GrantPermissions
            }
            alreadyDeclined -> {
                dialogEvent.value = DialogEvent.GrantPermissionsManually
            }
            else -> {
                requestPermissionLauncher.launch(permissions.keys.toTypedArray())
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openBluetoothSettings() {
        startActivity(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
    }

    private fun openAppSettings() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts(SCHEME, packageName, null)
            }
        )
    }

    companion object {
        private const val SCHEME = "package"
    }
}

private sealed class DialogEvent(
    @StringRes val title: Int = ResourcesCompat.ID_NULL,
    @StringRes val message: Int = ResourcesCompat.ID_NULL,
    @StringRes val confirmText: Int = R.string.ok
) {
    data object Idle : DialogEvent()
    data object FeatureError : DialogEvent(
        R.string.bluetooth_feature_missing_title, R.string.bluetooth_feature_missing_message
    )
    data object GrantPermissions : DialogEvent(
        R.string.permission_required_title, R.string.permission_required_message
    )
    data object GrantPermissionsManually : DialogEvent(
        R.string.permission_required_title, R.string.permission_required_message
    )
}

@Composable
private fun ZebraAlertDialog(event: DialogEvent, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(text = stringResource(event.title)) },
        text = { Text(stringResource(event.message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(event.confirmText))
            }
        }
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
