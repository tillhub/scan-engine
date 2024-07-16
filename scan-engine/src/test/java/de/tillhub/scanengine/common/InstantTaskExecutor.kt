package de.tillhub.scanengine.common

import android.annotation.SuppressLint
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor

@SuppressLint("RestrictedApi")
class InstantTaskExecutor {

    private val taskExecutor: ArchTaskExecutor by lazy {
        ArchTaskExecutor.getInstance()
    }

    fun setupLiveData() {
        taskExecutor.setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) {
                runnable.run()
            }

            override fun postToMainThread(runnable: Runnable) {
                runnable.run()
            }

            override fun isMainThread(): Boolean {
                return true
            }
        })
    }

    fun resetLiveData() {
        taskExecutor.setDelegate(null)
    }
}
