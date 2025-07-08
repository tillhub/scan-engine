package de.tillhub.scanengine

class AndroidPlatform : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
    override val manufacturer: String = android.os.Build.MANUFACTURER
}

actual fun getPlatform(): Platform = AndroidPlatform()
