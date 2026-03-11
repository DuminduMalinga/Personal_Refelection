plugins {
    alias(libs.plugins.android.application)
    // Add the Google services Gradle plugin
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.personal_refelection"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.personal_refelection"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    // Google Sign-In (proven, works on all devices with Play Services)
    implementation(libs.play.services.auth)
    // Facebook Login SDK
    implementation(libs.facebook.android.sdk)
    // Firebase - Import the Firebase BoM (version 34.10.0)
    implementation(platform(libs.firebase.bom))
    // Firebase Auth — for Google + Facebook sign-in
    implementation(libs.firebase.auth)
    // Firebase Analytics
    implementation(libs.firebase.analytics)
    // Glide — load Google profile photo from URL
    implementation(libs.glide)
    annotationProcessor(libs.glide.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}