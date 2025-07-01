package de.tillhub.scanengine.camera.common

import platform.darwin.DISPATCH_QUEUE_PRIORITY_HIGH
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

/**
 * Dispatch a block on the main queue.
 * @param block The block to dispatch.
 * @param priority The priority of the dispatch queue.
 */
fun dispatchAsync(priority: Int = DISPATCH_QUEUE_PRIORITY_HIGH, block: () -> Unit) {
    dispatch_async(
        queue = dispatch_get_global_queue(
            identifier = priority.toLong(),
            flags = 0u
        ),
        block = block
    )
}