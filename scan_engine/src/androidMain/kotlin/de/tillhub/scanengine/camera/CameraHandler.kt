package de.tillhub.scanengine.camera

import android.content.Context
import androidx.camera.lifecycle.ProcessCameraProvider
import de.tillhub.scanengine.camera.common.CameraProvider
import de.tillhub.scanengine.camera.common.CameraProviderImpl
import java.util.concurrent.Executor

internal interface CameraHandler {
    fun getCameraProvider(callback: (cameraProvider: CameraProvider) -> Unit)
}

internal class CameraHandlerImpl(
    private val context: Context,
    private val executor: Executor
) : CameraHandler{
    override fun getCameraProvider(callback: (cameraProvider: CameraProvider) -> Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            { callback(CameraProviderImpl(cameraProviderFuture.get())) },
            executor,
        )
    }
}