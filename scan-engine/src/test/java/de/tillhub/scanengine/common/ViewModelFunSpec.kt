package de.tillhub.scanengine.common

import io.kotest.core.listeners.TestListener
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
abstract class ViewModelFunSpec(
    body: FunSpec.() -> Unit = {}
) : FunSpec(body) {

    override fun listeners(): List<TestListener> {
        return listOf(ViewModelListener())
    }
}
