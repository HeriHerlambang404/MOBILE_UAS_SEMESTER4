plugins {
    alias(libs.plugins.android.application)
}

val roomVersion = "2.6.1"
val recyclerViewVersion = "1.3.2"
val cardViewVersion = "1.0.0"

android {
    namespace = "com.apk.catatkeuanganku"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.apk.catatkeuanganku"
        minSdk = 28
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // --- LIBRARY STATISTIK (MPAndroidChart) ---
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // --- ANDROIDX & UI ---
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation("androidx.recyclerview:recyclerview:$recyclerViewVersion")
    implementation("androidx.cardview:cardview:$cardViewVersion")

    // --- DATABASE ROOM ---
    implementation("androidx.room:room-runtime:$roomVersion")
    annotationProcessor("androidx.room:room-compiler:$roomVersion")

    // --- OTHERS ---
    implementation("com.hbb20:ccp:2.7.2")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation(libs.generativeai)
    implementation(libs.guava)

    // --- TESTING ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}