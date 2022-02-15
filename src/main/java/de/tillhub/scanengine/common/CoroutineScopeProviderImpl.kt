package de.tillhub.scanengine.common

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject

class CoroutineScopeProviderImpl @Inject constructor() : CoroutineScopeProvider {
    override val applicationScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
}
