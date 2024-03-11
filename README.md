
[![](https://jitpack.io/v/tillhub/scan-engine.svg)](https://jitpack.io/#tillhub/scan-engine)
[![API](https://img.shields.io/badge/API-24%2B-green.svg?style=flat)](https://android-arsenal.com/api?level-11)
# Scan Engine

This library combines various scanning protocols into a single, easy-to-use interface.

# How to setup

**Step 1.** Add the JitPack repository to your `settings.gradle` file:

```groovy
dependencyResolutionManagement {
    repositories {
        ...
        mavenCentral()
		maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency to your app `build.gradle`:
```groovy
dependencies {
    implementation 'com.github.tillhub:scan-engine:x.x.x'
}
```
# Usage

There are two ways you can use scan SDK:
* `Camera scanner` : Manually register an activity or fragment result callback for the camera scanner.
* `Barcode scanner` : Register a BroadcastReceiver with the barcode scanner.

### 1. Camera Scanner (Activity/Fragment):

*  `Singleton Access`: Use `@Inject lateinit var scanEngine: ScanEngine` to obtain a singleton reference to the Scan Engine instance.
*  `Initialization`: Create a per Activity or Fragment Scanner instance using `scanEngine.newCameraScanner(this)`. This method automatically selects the appropriate scanner based on the device manufacturer (SunmiScanner or DefaultScanner).
*  `Initiate Scan`: Call `scanner.startCameraScanner()` to initiate scanning. Pass appropriate scan key if needed, by default its null.
*  `Handle Scan Results`: Subscribe to the `scanEngine.observeScannerResults()` flow to receive ScanEvent objects containing scan data.

```kotlin

@Inject lateinit var scanEngine: ScanEngine

override fun onCreate(savedInstanceState: Bundle?) {
    // ...

    val scanner = scanEngine.newCameraScanner(this)
    
    scannerButton.setOnClickListener {
        scanner.startCameraScanner()
    }

    // Observing scan result with scanEngine or scanner
    scanEngine.observeScannerResults().collect { scanEvent ->
         // Handle scanEvent result
    }
    // OR
    scanner.observeScannerResults().collect { scanEvent ->
        // Handle scanEvent result
    }
}
```

### 2. Barcode Scanner (BroadcastReceiver):

*  `Singleton Access`: Use `@Inject lateinit var scanEngine: ScanEngine` to obtain a singleton reference to the Scan Engine instance.
*  `Initiate Scan`: Call `scanEngine.barcodeScanner.scanWithKey()` to trigger a background scan using the system's broadcast mechanism. Pass appropriate scan key if needed, by default its null.
*  `Handle Scan Results`: Subscribe to `scanEngine.observeScannerResults()` for ScanEvent objects.

```kotlin
@Inject lateinit var scanEngine: ScanEngine

override fun onCreate(savedInstanceState: Bundle?) {
    // ...

    scanEngine.barcodeScanner.scanWithKey()

    // Observing scan result with scanEngine
    scanEngine.observeScannerResults().collect { scanEvent ->
        // Handle scanEvent result
    }
    
}
```

Here's example of `Scanner` interface and what it provides:

```kotlin
interface Scanner {
    fun observeScannerResults(): SharedFlow<ScanEvent>

    fun startCameraScanner(scanKey: String? = null)
}
```

## Credits

- [Đorđe Hrnjez](https://github.com/djordjeh)
- [Martin Širok](https://github.com/SloInfinity)
- [Chandrashekar Allam](https://github.com/shekar-allam)

## License

```licence
MIT License

Copyright (c) 2024 Tillhub GmbH

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
