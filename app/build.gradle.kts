plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
    alias(libs.plugins.google.firebase.crashlytics)
}

android {
    namespace = "com.HG.heroesglory"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.HG.heroesglory"
        minSdk = 26
        targetSdk = 35
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

    implementation(platform("com.google.firebase:firebase-bom:34.2.0"))
    implementation("com.google.firebase:firebase-analytics")

    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.github.rubensousa:gravitysnaphelper:2.2.2")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.google.firebase:firebase-firestore:24.9.1")
    implementation("androidx.cardview:cardview:1.0.0")

    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    // Database (Room)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.firebase.crashlytics)
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Firebase BoM (Bill of Materials) для согласованных версий
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-firestore") // Firestore
    implementation("com.google.firebase:firebase-auth") // Auth (на будущее)

    // Image Loading
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // Lottie for animations
    implementation("com.airbnb.android:lottie:6.3.0")

    // JSON Parsing (Gson)
    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}