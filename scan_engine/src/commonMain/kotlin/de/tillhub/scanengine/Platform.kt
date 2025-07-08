package de.tillhub.scanengine

interface Platform {
    val name: String
    val manufacturer: String
}

expect fun getPlatform(): Platform
