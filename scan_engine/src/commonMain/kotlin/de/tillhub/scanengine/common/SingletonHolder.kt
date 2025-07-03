package de.tillhub.scanengine.common

import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.SynchronizedObject
import kotlinx.coroutines.internal.synchronized
import kotlin.concurrent.Volatile

/**
 * A generic class for creating and managing singleton instances.
 * This class ensures that only one instance of a given type [T] is created and provides a thread-safe way to access it.
 *
 * @param T The type of the singleton instance. It must be a non-nullable type that inherits from [Any].
 * @property creator A lambda function that creates the instance of [T]. This function is invoked only once, the first time [getInstance] is called.
 *                  After the instance is created, this property is set to `null` to release the reference to the creator function.
 * @property instance The singleton instance of type [T]. It is initialized lazily when [getInstance] is called for the first time.
 *                   This property is marked as `@Volatile` to ensure visibility across threads.
 *
 * This class inherits from [SynchronizedObject] and uses its `synchronized` block to ensure thread-safe initialization of the singleton instance.
 * The `@OptIn(InternalCoroutinesApi::class)` annotation is used because [SynchronizedObject] and its `synchronized` function are part of Kotlin's internal coroutines API.
 */
@OptIn(InternalCoroutinesApi::class)
open class SingletonHolder<out T : Any>(creator: () -> T) : SynchronizedObject() {
    private var creator: (() -> T)? = creator
    @Volatile
    private var instance: T? = null

    fun getInstance(): T {
        val checkInstance = instance
        if (checkInstance != null) {
            return checkInstance
        }

        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null) {
                checkInstanceAgain
            } else {
                val created = creator!!()
                instance = created
                creator = null
                created
            }
        }
    }
}
