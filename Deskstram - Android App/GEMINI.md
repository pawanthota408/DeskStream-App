# DeskStream — Android Studio Project Context & Agent Guidelines

## Overview
DeskStream is a modern Android application built using **Jetpack Compose** and **Material 3**. It provides seamless desktop streaming, remote device management, file transfers, and notification synchronization.

- **Package Name**: `com.deskstream`
- **Minimum SDK**: 24 (Android 7.0)
- **Target / Compile SDK**: 37 (Android 16 Baklava / API 35+ compatible)
- **Primary Language**: Kotlin (Kotlin 2.1+ with Jetpack Compose compiler)
- **UI Toolkit**: Jetpack Compose with Material3
- **Splash Screen**: `androidx.core.splashscreen`

---

## Architecture & Code Structure
The application follows modern Android architecture principles:

```
app/src/main/java/com/deskstream/
├── MainActivity.kt               # Single Activity entry point with Edge-to-Edge & screen navigation
└── ui/
    ├── home/
    │   ├── HomeScreen.kt          # Main dashboard with side drawer & navigation tabs
    │   ├── DevicesScreen.kt       # Remote devices management
    │   ├── FilesScreen.kt         # Remote & local file explorer
    │   ├── SharedScreen.kt        # Shared drives & resources
    │   ├── NotificationsScreen.kt # Alert & message center
    │   ├── ProfileScreen.kt       # User profile & account management
    │   └── SettingsScreen.kt      # Application & connection preferences
    ├── login/
    │   └── LoginScreen.kt         # Authentication & login flow
    ├── splash/
    │   └── SplashScreen.kt        # Animated splash screen
    └── theme/
        ├── Color.kt               # DeskStream color tokens
        ├── Theme.kt               # DeskStreamTheme composable
        └── Type.kt                # Typography scale
```

---

## Theme & Design Guidelines
- **Brand Colors**:
  - Primary Accent: Red (`#DC2626` / `#B91C1C`)
  - Background: Crisp Light Slate (`#F8FAFC`)
  - Dark Elements: Slate Dark (`#111827`)
  - Surface: White (`#FFFFFF`)
- **Typography**: Clean, readable sans-serif typography defined in `Type.kt`.
- **Edge-to-Edge**: Always preserve `enableEdgeToEdge()` in `MainActivity`.

---

## Android Studio & Agent Instructions
When writing or refactoring code for this project:
1. **Always use Jetpack Compose and Material 3** (`androidx.compose.material3.*`).
2. **Never import legacy View-based widgets** or deprecated AppCompat themes.
3. **Keep dependency versions centralized** in `gradle/libs.versions.toml`.
4. **Follow Edge-to-Edge window insets**: Use `Modifier.statusBarsPadding()`, `Modifier.navigationBarsPadding()`, or `Scaffold` inner padding for correct layout rendering on modern Android devices.
