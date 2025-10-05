plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.car_login"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.car_login"
        minSdk = 34
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
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
    // --- THIS IS THE CORRECTED AND CLEANED DEPENDENCIES BLOCK ---

    // Use the aliases from your libs.versions.toml file
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Use the Google Maps alias (ensure it's in your libs.versions.toml)
    implementation(libs.play.services.maps)

    // REMOVED: Redundant hardcoded dependencies
    // implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    // implementation("com.google.android.material:material:1.12.0")
    // implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Test dependencies remain
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
