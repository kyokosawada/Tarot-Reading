import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.tarot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.tarot.demo"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Add BuildConfig fields for API keys
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        
        buildConfigField("String", "OPENAI_API_KEY", "\"${localProperties.getProperty("OPENAI_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Temporarily disable for successful build
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
        buildConfig = true // Enable BuildConfig generation
    }

    lint {
        disable.add("NullSafeMutableLiveData")
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.material.icons.extended)
    
    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Koin - Replace Hilt
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)
    implementation(libs.koin.androidx.navigation)
    
    // DataStore for settings
    implementation(libs.androidx.datastore)
    
    // Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:33.16.0"))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    // Removed firebase analytics
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)

    // OpenAI API and HTTP client
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Camera X dependencies for palm reading
    implementation(libs.camera.core)
    implementation(libs.camera.camera2)
    implementation(libs.camera.lifecycle)
    implementation(libs.camera.view)

    // Modern permissions - Using ActivityResultContracts (no Accompanist needed)

    // Image processing
    implementation(libs.androidx.exifinterface)

    // Modern image loading with Coil 3.x
    implementation("io.coil-kt.coil3:coil-compose:3.2.0")
    implementation("io.coil-kt.coil3:coil-network-okhttp:3.2.0")

    debugImplementation(libs.androidx.ui.tooling)
}