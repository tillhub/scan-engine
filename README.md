
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

This SDK offers two ways to scan products conveniently:
* `Camera scanner(SunmiScanner/DefaultScanner)` : Manually register an activity or fragment result callback.
* `Barcode scanner(BarcodeScannerImpl)` : Register a BroadcastReceiver with the barcode scanner.

### 1. Camera Scanner :

*  `Singleton Access`: Obtain a singleton reference to the `ScanEngine` instance.
*  `Initialization`: Create a per Activity or Fragment Scanner instance. SDK will automatically selects the appropriate scanner based on the device manufacturer (SunmiScanner or DefaultScanner).
*  `Initiate Scan`: Call `scanner.startCameraScanner()` to initiate scanning. Pass appropriate scan key if needed, by default its null.
*  `Handle Scan Results`: Subscribe to the `scanEngine.observeScannerResults()` flow to receive ScanEvent objects containing scan data.

```kotlin

override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    val scanEngine =  ScanEngine.getInstance(context)
    val scanner = scanEngine.newCameraScanner(this)
    
    scannerButton.setOnClickListener {
        scanner.startCameraScanner()
    }

    // Observing scan result with scanEngine
    scanEngine.observeScannerResults().collect { scanEvent ->
         // Handle scanEvent result
    }
}
```

Here's example of `Scanner` interface:

```kotlin
interface Scanner {
    fun observeScannerResults(): StateFlow<ScanEvent>
    fun startCameraScanner(scanKey: String? = null)
}
```

### 2. Barcode Scanner :

*  `Singleton Access`: Obtain a singleton reference to the `ScanEngine` instance. This will trigger a background scan using the system's broadcast mechanism.
*  `Handle Scan Results`: Subscribe to `scanEngine.observeScannerResults()` for ScanEvent objects  containing scan data.

```kotlin

override fun onCreate(savedInstanceState: Bundle?) {
    // ...
    
    val scanEngine =  ScanEngine.getInstance(context)
    
    // Observing scan result with scanEngine
    scanEngine.observeScannerResults().collect { scanEvent ->
        // Handle scanEvent result
    }
    
}
```

Here's example of `BarcodeScanner` interface:

```kotlin
interface BarcodeScanner {
    fun observeScannerResults(): Flow<ScanEvent>
    fun scanWithKey(scanKey: String? = null)
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
