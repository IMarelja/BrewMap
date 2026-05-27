import java.io.File

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

// Read BIND_API_URL from the repository root .env (../../.env from this module).
// Build fails fast if the file or key is missing, so incorrect environments never compile.
val envFile = rootProject.file("../../../.env")
if (!envFile.exists()) {
    error(".env file not found at ${envFile.absolutePath}. Create it and set BIND_API_URL.")
}

fun readEnvValue(file: File, key: String): String? {
    return file.readLines()
        .asSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val idx = line.indexOf('=')
            if (idx <= 0) return@mapNotNull null
            val k = line.substring(0, idx).trim()
            val rawValue = line.substring(idx + 1).trim()
            if (k != key) return@mapNotNull null
            rawValue.substringBefore("#").trim().trim('"').trim('\'')
        }
        .firstOrNull()
}

val bindApiUrl: String = readEnvValue(envFile, "BIND_API_URL")
    ?.takeIf { it.isNotBlank() }
    ?: error(
        "BIND_API_URL is missing or empty in ${envFile.absolutePath}. " +
        "Add for example: BIND_API_URL=http://10.0.2.2:5239/api/"
    )

val validAppModes = listOf("HARD_CODE", "API", "PERSISTENT")
val appMode: String = readEnvValue(envFile, "APP_MODE")
    ?.takeIf { it.isNotBlank() }
    ?.also { mode ->
        if (mode !in validAppModes) {
            error(
                "APP_MODE='$mode' in ${envFile.absolutePath} is not valid. " +
                "Allowed values: ${validAppModes.joinToString()}"
            )
        }
    }
    ?: error(
        "APP_MODE is missing or empty in ${envFile.absolutePath}. " +
        "Add one of: APP_MODE=HARD_CODE | API | PERSISTENT"
    )

android {
    namespace = "hr.algebra.mobileapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "hr.algebra.mobileapp"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Exposed as BuildConfig.API_BASE_URL, sourced from root .env:BIND_API_URL.
        buildConfigField("String", "API_BASE_URL", "\"$bindApiUrl\"")
        // Exposed as BuildConfig.APP_MODE, sourced from root .env:APP_MODE.
        buildConfigField("String", "APP_MODE", "\"$appMode\"")
    }

    buildFeatures {
        buildConfig = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.fragment:fragment-ktx:1.8.6")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation(libs.retrofit)
    implementation(libs.adapter.rxjava2)
    implementation(libs.converter.gson)
    implementation(libs.rxandroid)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.android)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.kotlinx.serialization.json)
    // Token storage — Android Keystore-backed EncryptedSharedPreferences
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    // Lifecycle-aware coroutine scopes (lifecycleScope in fragments/activities)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    // OpenStreetMap rendering
    implementation("org.osmdroid:osmdroid-android:6.1.20")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
