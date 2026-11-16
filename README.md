# HyperLauncher

A minimal, HyperOS-inspired Android home launcher (Kotlin + Jetpack Compose + Material 3).

- Package: `com.gafure.hyperlauncher`
- minSdk 31, targetSdk 34, JDK 17, Gradle 8.7, AGP 8.5.2

## Build

```bash
./gradlew assembleDebug        # app/build/outputs/apk/debug/app-debug.apk
./gradlew assembleRelease      # release APK, R8 + resource shrinking
./gradlew assembleStaging      # release-like build, installs side-by-side with debug
./gradlew testDebugUnitTest    # JVM unit tests
```

`ANDROID_HOME` (or `local.properties` with `sdk.dir`) must point at an SDK with
platform 34 and build-tools 34.0.0.

## Architecture

```
app/src/main/java/com/gafure/hyperlauncher/
├── HyperLauncherApp.kt          Application: process-wide AppRepository
├── MainActivity.kt              HOME activity, edge-to-edge, theme from settings
├── model/AppInfo.kt             immutable UI model (label, package, ImageBitmap)
├── data/
│   ├── AppRepository.kt         PackageManager queries, launch, package broadcasts
│   ├── AppListLogic.kt          pure sorting / dedupe / dock picking (unit-tested)
│   ├── AppIconCache.kt          bounded icon decode cache, off-main-thread
│   ├── HomeLayoutStore.kt       DataStore: which apps live on the home screen
│   └── settings/                LauncherSettings, SettingsStore, pure mapper
└── ui/
    ├── HomeViewModel.kt         combine(apps, layout, settings) -> StateFlow<HomeUiState>
    ├── HyperHomeScreen.kt       scrim, clock, pager, dock, drawer, settings sheet
    ├── SettingsSheet.kt         every control writes real persisted state
    ├── components/              AppGridPager, AppGridCell, AppIcon, AppDrawer, dock, clock
    ├── shape/SquircleShape.kt   superellipse icon mask
    └── theme/                   colors, typography, dimens, theme mode resolution
```

Settings (all persisted in DataStore and applied): grid columns, drawer columns, icon
size, app labels, theme (system/light/dark), animation speed, wallpaper scrim strength.

## Tests

```bash
./gradlew testDebugUnitTest
```

Covers app-list shaping (sorting, own-package filtering, dedupe, dock picking), the home
layout store, and settings mapping/clamping. Background work is off the main thread:
PackageManager queries and icon decoding run on `Dispatchers.Default`, DataStore on its own
scope.

## Release signing

Release builds are intentionally **unsigned by a debug key only when no release
keystore is configured**. Official APKs must be signed in CI from secrets —
never commit a keystore or passwords:

1. Create a keystore and add it plus its credentials as GitHub Actions secrets
   (e.g. `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`,
   `RELEASE_STORE_PASSWORD`).
2. Wire them into a `release` signing config in `app/build.gradle.kts`.
3. Tag the commit (`git tag vX.Y.Z`) — the workflow publishes GitHub Releases
   **only** from `v*` tags, never from branch pushes.

Switching between debug-signed and release-signed APKs with the same package
name requires uninstalling first (`INSTALL_FAILED_UPDATE_INCOMPATIBLE`).
Use the `staging` build type (applicationId `...hyperlauncher.staging`) to test
release behaviour without uninstalling debug.
