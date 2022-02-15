package de.tillhub.scanengine.common

import kotlinx.coroutines.CoroutineScope

interface CoroutineScopeProvider {
    val applicationScope: CoroutineScope
}
