# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keeppackagenames de.tillhub.scanengine.**

-keep class de.tillhub.scanengine.CameraScanner { *; }
-keep class de.tillhub.scanengine.ScanEngine { *; }
-keep class de.tillhub.scanengine.ScanEngine$Companion { *; }
-keep class de.tillhub.scanengine.common.SingletonHolder { *; }

-keep class de.tillhub.scanengine.data.ScanEvent { *; }
-keep class de.tillhub.scanengine.data.ScanEvent* { *; }
-keep class de.tillhub.scanengine.data.Scanner { *; }
-keep class de.tillhub.scanengine.data.ScannerResponse { *; }
-keep class de.tillhub.scanengine.data.ScannerResponse* { *; }
-keep class de.tillhub.scanengine.data.ScannerManufacturer { *; }
-keep class de.tillhub.scanengine.BarcodeScanner {  *; }

# Breaking changes with AGP 8.0
# R8 upgrade documentation
-dontwarn java.lang.invoke.StringConcatFactory
