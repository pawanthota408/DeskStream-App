# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in getDefaultProguardFile('proguard-android-optimize.txt')

# Keep Kotlin reflect and coroutines metadata if used
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

# Keep Jetpack Compose classes and view models
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# Keep Deskstream models
-keep class com.deskstream.** { *; }
