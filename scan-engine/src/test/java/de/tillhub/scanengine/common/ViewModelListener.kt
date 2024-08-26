package de.tillhub.scanengine.common

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.Spec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@ExperimentalCoroutinesApi
class ViewModelListener(
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher(),
    private val instantTaskExecutor: InstantTaskExecutor = InstantTaskExecutor()
) : TestListener {

    override suspend fun beforeSpec(spec: Spec) {
        Dispatchers.setMain(testDispatcher)
        instantTaskExecutor.setupLiveData()
    }

    override suspend fun afterSpec(spec: Spec) {
        Dispatchers.resetMain()
        instantTaskExecutor.resetLiveData()
    }
}
