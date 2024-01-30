package de.tillhub.scanengine.google.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import de.tillhub.scanengine.R
import de.tillhub.scanengine.databinding.ActivityGoogleScanningBinding
import java.util.concurrent.Executor

class GoogleScanningActivity : AppCompatActivity() {

    private val viewModel: GoogleScanningViewModel by viewModels { GoogleScanningViewModel.Factory }
    private val binding by viewBinding(ActivityGoogleScanningBinding::inflate)

    private lateinit var camera: Camera

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            bindCamera()
        } else {
            Toast.makeText(
                baseContext,
                R.string.error_permission_not_granted,
                Toast.LENGTH_SHORT
            ).show()
            binding.requestPermission.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        binding.requestPermission.setOnClickListener {
            binding.requestPermission.isGone = true
            checkPermission()
        }

        lifecycleScope.launchWhenStarted {
            viewModel.scanningState.collect { state ->
                when (state) {
                    is ScanningState.CodeScanned -> {
                        setResult(
                            RESULT_OK,
                            Intent().also {
                                it.putExtra(DATA_KEY, state.barcode)
                            }
                        )
                        finish()
                    }

                    ScanningState.Idle -> Unit
                }
            }
        }

        checkPermission()
    }

    private fun checkPermission() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                baseContext,
                Manifest.permission.CAMERA
            ) -> bindCamera()

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun bindCamera() {
        val executor = ContextCompat.getMainExecutor(applicationContext)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(applicationContext)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()
                bindPreview(cameraProvider, executor)
            },
            executor
        )
    }

    private fun bindPreview(cameraProvider: ProcessCameraProvider, cameraExecutor: Executor) {
        val preview: Preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor, viewModel.analyzer)
            }

        cameraProvider.unbindAll()

        camera = cameraProvider.bindToLifecycle(
            this as LifecycleOwner,
            cameraSelector,
            imageAnalyzer,
            preview
        )
    }

    companion object {
        const val DATA_KEY = "scanned_data"
    }
}

inline fun <T : ViewBinding> AppCompatActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T,
) = lazy(LazyThreadSafetyMode.NONE) { bindingInflater.invoke(layoutInflater) }
