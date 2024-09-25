package de.tillhub.scanengine.zebra

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import de.tillhub.scanengine.R
import de.tillhub.scanengine.extensions.getModifierBasedOnDeviceType
import de.tillhub.scanengine.theme.OrbitalBlue
import de.tillhub.scanengine.theme.ScanEngineTheme
import de.tillhub.scanengine.theme.TabletScaffoldModifier
import de.tillhub.scanengine.theme.Toolbar
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

    private val registerForResult = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.initScanner()
        } else {
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiStateFlow.collectAsState()
            ScanEngineTheme {
                Scaffold(
                    modifier = getModifierBasedOnDeviceType(
                        isTablet = TabletScaffoldModifier,
                        isMobile = Modifier
                    ),
                    topBar = {
                        Toolbar(title = stringResource(id = R.string.pairing_title)) {
                            finish()
                        }
                    }
                ) {
                    ZebraPairBarcodeActivityContent(
                        activity = this,
                        paddingValues = it,
                        state = state,
                    )
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

        checkBluetoothAccessibility()
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
        registerForResult.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
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

@Preview
@Composable
private fun ZebraAlertDialog(
    event: DialogEvent = DialogEvent.GrantPermissions,
    onConfirm: () -> Unit = {}
) {
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
private fun ZebraPairBarcodeActivityContent(
    activity: Activity,
    paddingValues: PaddingValues = PaddingValues(),
    state: ZebraPairBarcodeViewModel.State = ZebraPairBarcodeViewModel.State.Loading,
) {
    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (state) {
            ZebraPairBarcodeViewModel.State.Connected -> {
                Toast.makeText(activity, R.string.pairing_successful, Toast.LENGTH_SHORT).show()
                activity.finish()
            }

            ZebraPairBarcodeViewModel.State.Loading -> {
                Text(stringResource(R.string.loading))
            }

            is ZebraPairBarcodeViewModel.State.Pairing -> {
                state.result.onSuccess {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(id = R.string.pairing_instruction),
                            modifier = Modifier.padding(16.dp)
                        )
                        Image(
                            painter = painterResource(id = R.drawable.classic_discoverable),
                            contentDescription = "Error",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                        )
                        BluetoothSettingsButton {
                            activity.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
                            activity.finish()
                        }
                    }
                }
                state.result.onFailure {
                    Toast.makeText(activity, it.message, Toast.LENGTH_SHORT).show()
                    activity.finish()
                }
            }
        }
    }
}

@Composable
@Preview
internal fun BluetoothSettingsButton(
    onClick: () -> Unit = {},
) {
    Button(
        enabled = true,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(4.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = OrbitalBlue
        )
    ) {
        Text(
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.wrapContentHeight(),
            text = stringResource(id = R.string.title_bluetooth_setting)
        )
    }
}
