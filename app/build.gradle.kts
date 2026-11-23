plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.electrosathi.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.electrosathi.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
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
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.0")

    // Batch 14 (Offline Core): the Firebase + Google sign-in libraries
    // are GONE. The PS4 mentoring rule is 100% offline — accounts,
    // content and progress all live in the app's on-device SQLite
    // database (see data/AppDb.kt). No cloud SDK ships in the APK.
    // (The google-services plugin stays applied but now does nothing
    // more than generate an unused resource file; removing the plugin
    // itself is a Phase 2 tidy-up.)

    // Volt's on-device brain (Batch 9) — ExecuTorch runtime + Llama LLM API.
    // Pinned to 1.1.0: the exact version Meta's LlamaDemo ships, proven
    // on-device in Step 0 (12 Nov 2026) with our SpinQuant 1B model file.
    implementation("org.pytorch:executorch-android:1.1.0")
    implementation("com.facebook.fbjni:fbjni:0.5.1")
    implementation("com.google.mlkit:text-recognition:16.0.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
