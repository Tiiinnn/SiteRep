# SiteRep

SiteRep is an offline-first Android application for creating, managing, and sharing daily construction site reports. It keeps unit details and report history on the device, so reports can be prepared without an internet connection and copied directly into messaging, email, or other work tools.

## Highlights

- Create and manage construction units with Block/Lot, project, and location details.
- Build daily site reports with a real date picker, weather notes, manpower controls, activities, and remarks.
- Automatically calculate total manpower from six worker categories.
- Preserve paragraphs, headings, numbered lists, bullets, blank lines, and other multiline formatting in generated reports.
- Generate, save, and copy reports to the Android clipboard in one action.
- Browse reports by daily, weekly, or monthly grouping; copy a compiled group when needed.
- Open, edit, copy again, or delete saved reports without changing the original unit defaults.
- Store all data locally using Room; a clean installation starts with no sample units or reports.
- Use a clean white Material 3 interface designed for touch use, keyboards, and standard Android system bars.

## Tech stack

- Kotlin
- Jetpack Compose and Material 3
- Compose Navigation
- Room database
- AndroidX SplashScreen

## Requirements

To build the project locally, install:

- Android Studio (latest stable recommended)
- Android SDK Platform 36 and Build Tools 36.1.0
- JDK 17 (Android Studio's embedded JDK is supported)
- Git

The app has a minimum Android version of API 26 (Android 8.0).

## Clone the project

```powershell
git clone <YOUR-REPOSITORY-URL> SiteRep
cd SiteRep
```

Replace `<YOUR-REPOSITORY-URL>` with the repository URL you were given. Then open the `SiteRep` folder in Android Studio and allow Gradle to sync.

## Run from Android Studio

1. Open the `SiteRep` folder in Android Studio.
2. Wait for Gradle sync to finish and install any requested Android SDK components.
3. Connect an Android device with USB debugging enabled, or create an emulator running Android 8.0 or later.
4. Select the `app` run configuration and press **Run**.

## Build a debug APK

The debug APK is suitable for development and testing:

```powershell
.\gradlew.bat clean assembleDebug
```

Output:

`app/build/outputs/apk/debug/app-debug.apk`

## Build a signed release APK

Release APKs are optimized, signed, and suitable for distribution outside Google Play. The repository does not include private signing passwords or keystores.

1. Create a release keystore if you do not already have one. Keep it securely backed up; the same key is required for all future updates.

```powershell
keytool -genkeypair -v -keystore signing/siterep-release.jks -alias siterep -keyalg RSA -keysize 4096 -validity 10000
```

2. Copy the signing configuration template and fill in your own values:

```powershell
Copy-Item keystore.properties.example keystore.properties
```

3. Edit `keystore.properties`. Do not commit this file or share its passwords. Example:

```properties
storeFile=signing/siterep-release.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=siterep
keyPassword=YOUR_KEY_PASSWORD
```

4. Run tests, lint, and the release build:

```powershell
.\gradlew.bat --stop
.\gradlew.bat --no-daemon clean testDebugUnitTest lintDebug assembleRelease --console=plain
```

The signed release APK is generated at:

`app/build/outputs/apk/release/app-release.apk`

## Verify the release signature

Use `apksigner` from your installed Android SDK Build Tools:

```powershell
<ANDROID_SDK>\build-tools\36.1.0\apksigner.bat verify --verbose --print-certs app\build\outputs\apk\release\app-release.apk
```

Replace `<ANDROID_SDK>` with your SDK folder, such as `C:\Users\your-name\AppData\Local\Android\Sdk`.

## Install an APK on a device

With Android platform tools installed and USB debugging enabled:

```powershell
adb install -r app\build\outputs\apk\release\app-release.apk
```

Use the debug APK path instead if you built a debug version.

## Data and privacy

SiteRep is offline-first. Units and reports are stored only in the device's local Room database. Uninstalling the app or clearing its storage removes that local data unless it has been manually copied or backed up.

## License and copyright

Copyright © 2026 Juztinn Cepillo. All rights reserved.

SiteRep was created to make daily site reporting faster, clearer, and easier to manage. This repository's source code and release assets may not be copied, redistributed, modified, or used commercially without written permission from the copyright holder.
