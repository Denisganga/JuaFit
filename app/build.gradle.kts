plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val juaBackendUrl = providers.gradleProperty("JUA_BACKEND_URL").orElse(providers.environmentVariable("JUA_BACKEND_URL")).getOrElse("")

android {
    namespace = "com.pathwayfit.app"
    compileSdk = 36

    defaultConfig {
        buildConfigField("boolean", "JUA_DEMO_MODE", providers.gradleProperty("JUA_DEMO_MODE").getOrElse(if (juaBackendUrl.isBlank()) "true" else "false"))
        applicationId = "com.pathwayfit.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-prototype"
        vectorDrawables { useSupportLibrary = true }

        // Feature flags. Payment stays OFF for the prototype.
        buildConfigField("boolean", "PAYMENT_ENABLED", "false")
    }

    buildTypes {
        debug {
            buildConfigField("String", "GEMINI_ANALYSIS_URL", "\"${juaBackendUrl}\"")
            // Developer settings (e.g. "Enable Premium Preview") are only reachable in debug builds.
            buildConfigField("boolean", "DEV_TOOLS", "true")
        }
        release {
            buildConfigField("String", "GEMINI_ANALYSIS_URL", "\"${juaBackendUrl}\"")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            buildConfigField("boolean", "DEV_TOOLS", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

// Replaces android.kotlinOptions (removed with built-in Kotlin). jvmTarget follows compileOptions (17).
kotlin {
    compilerOptions {
        optIn.add("androidx.compose.material3.ExperimentalMaterial3Api")
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2025.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    // Icons are no longer released with the BOM; 1.7.8 is the final version.
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

    testImplementation("junit:junit:4.13.2")
    debugImplementation("androidx.compose.ui:ui-tooling")
}
