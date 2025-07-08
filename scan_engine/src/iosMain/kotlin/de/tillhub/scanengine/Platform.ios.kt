package de.tillhub.scanengine

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
    override val manufacturer: String = "apple"
}

actual fun getPlatform(): Platform = IOSPlatform()
