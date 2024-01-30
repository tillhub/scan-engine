package de.tillhub.scanengine.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class CoroutineScopeProviderImpl : CoroutineScopeProvider {
    override val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
