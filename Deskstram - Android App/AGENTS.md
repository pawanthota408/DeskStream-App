# Agent Instructions for DeskStream

## Workspace Configuration
- Project: **DeskStream Android Client**
- Language: **Kotlin 2.1+**
- Build System: **Gradle (Kotlin DSL - build.gradle.kts)**
- UI Framework: **Jetpack Compose + Material 3**

## Agent Operating Rules
1. **Gradle Files**: Always edit dependencies in `gradle/libs.versions.toml` and reference via `libs.*`.
2. **Package Hygiene**: App package is `com.deskstream`. Do NOT add foreign packages to this codebase.
3. **Android Studio Sync**: After any dependency change, invoke Gradle Sync via Android Studio UI (`File -> Sync Project with Gradle Files` or `Sync Project` toolbar button).
4. **Edge-to-Edge Compliance**: The app uses Android 15/16 edge-to-edge guidelines. Ensure all top app bars and bottom navigation bars respect system window insets.
