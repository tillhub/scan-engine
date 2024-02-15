import androidx.annotation.Keep
import java.lang.ref.WeakReference

open class SingletonHolder<out T : Any, in A>(val creator: (A) -> T) {
    private var currentActivity: WeakReference<A>? = null

    @Volatile
    private var instance: T? = null

    @Keep
    fun getInstance(arg: A): T {
        val checkInstance = instance
        if (checkInstance != null && currentActivity?.get() == arg) {
            return checkInstance
        }
        return synchronized(this) {
            val checkInstanceAgain = instance
            if (checkInstanceAgain != null && currentActivity?.get() == arg) {
                checkInstanceAgain
            } else {
                currentActivity = WeakReference(arg)
                val created = creator(arg)
                instance = created
                created
            }
        }
    }
}
