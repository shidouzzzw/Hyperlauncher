plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.gafure.hyperlauncher"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.gafure.hyperlauncher"
        minSdk = 31
        targetSdk = 34
        versionCode = 3
        versionName = "1.2.0-dev"
        vectorDrawables { useSupportLibrary = true }
    }

    // Optional real release signing: supply a keystore path + credentials through the
    // environment (CI secrets). Nothing secret is written into the repository. Without
    // them the release build falls back to the debug key so the APK still installs for
    // local verification — it is then NOT suitable for distribution.
    val releaseKeystorePath = System.getenv("RELEASE_KEYSTORE_FILE")
    if (releaseKeystorePath != null) {
        signingConfigs.create("release") {
            storeFile = file(releaseKeystorePath)
            storePassword = System.getenv("RELEASE_STORE_PASSWORD")
            keyAlias = System.getenv("RELEASE_KEY_ALIAS")
            keyPassword = System.getenv("RELEASE_KEY_PASSWORD")
        }
    }

    buildTypes {
        debug {
            // Debug key is fine for local iteration.
            signingConfig = signingConfigs.getByName("debug")
        }
        // Release-like build that installs side-by-side with debug (different
        // applicationId) so release-only behaviour can be verified on device.
        // It is still signed with the debug key; official releases need a real
        // keystore configured in CI secrets (see README).
        create("staging") {
            initWith(getByName("release"))
            applicationIdSuffix = ".staging"
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            signingConfig = signingConfigs.findByName("release")
                ?: signingConfigs.getByName("debug")
            // R8 on: smaller dex, faster class loading, no unused resources shipped.
            // NOTE: uses proguard-android.txt (no optimizer). The -optimize variant
            // rewrites bytecode aggressively and is a classic source of
            // release-only startup crashes; shrinking still removes dead code.
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.8")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("androidx.profileinstaller:profileinstaller:1.3.1")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
