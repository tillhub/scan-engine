package de.tillhub.scanengine.camera.common

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LifecycleOwner

internal interface CameraProvider {
    fun unbindAll()

    fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        vararg useCases: UseCase?,
    ): Camera
}

internal class CameraProviderImpl(
    private val cameraProvider: ProcessCameraProvider,
) : CameraProvider {
    override fun unbindAll() {
        cameraProvider.unbindAll()
    }

    override fun bindToLifecycle(
        lifecycleOwner: LifecycleOwner,
        cameraSelector: CameraSelector,
        vararg useCases: UseCase?,
    ): Camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, *useCases)
}
