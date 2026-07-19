# SiteRep for Android

A native, offline-first Android application for managing construction units and producing clipboard-ready daily site reports.

## Features

- Add, edit, select, and delete units.
- Create reports with a native date picker, weather, manpower steppers, activities, and remarks.
- Automatically calculate total manpower.
- Preserve multiline formatting in generated reports.
- Save report history locally with Room and group it by report date.
- Open and copy any saved report again.
- Start with an empty unit list on a fresh install.
- Use a clean white interface with immersive edge-to-edge display.

## Technology

- Kotlin and Jetpack Compose
- Material 3 and Compose Navigation
- Room database
- Minimum SDK 26; compile and target SDK 36

## Build

Open the directory in Android Studio and allow Gradle sync to complete, or run:

```powershell
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Production release

Release builds use R8 code optimization, obfuscation, and resource shrinking. Copy
`keystore.properties.example` to `keystore.properties`, provide the permanent
SiteRep keystore details, then run:

```powershell
.\gradlew.bat testDebugUnitTest lintRelease assembleRelease
```

Keep the release keystore and passwords in secure backups. The same signing key
is required for every future SiteRep update.

## Purpose

The purpose of this app is for the developer's friend and not meant for distribution unless ditributed by the App owner.
