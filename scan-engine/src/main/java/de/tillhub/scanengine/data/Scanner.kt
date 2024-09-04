package de.tillhub.scanengine.data

class Scanner(
    val id: String,
    val name: String,
    val serialNumber: String,
    val isConnected: Boolean,
    val modelNumber: String?,
    val fwVersion: String?
)
