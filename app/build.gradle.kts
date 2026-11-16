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
        versionCode = 2
        versionName = "1.1.0"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            // R8 on: smaller dex, faster class loading, no unused resources shipped.
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
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

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

composeCompiler {
    // Kotlin 2.0 enables strong skipping by default; keep it explicit so it cannot regress.
    enableStrongSkippingMode.set(true)
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
}
