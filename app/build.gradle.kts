plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.assignment"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.assignment"
        minSdk = 24
        targetSdk = 34
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

    // AndroidX Libraries
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    // Firebase platform BOM (Bill of Materials)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))

    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth-ktx")

    // Firebase Firestore
    implementation ("com.google.firebase:firebase-firestore:25.1.1")  // Update to the latest version
    implementation ("com.google.firebase:firebase-auth:23.1.0")       // For Firebase Authentication (if you're using it)
    implementation ("com.google.firebase:firebase-core:21.1.1")


    // WorkManager for background tasks
    implementation("androidx.work:work-runtime-ktx:2.8.1")

    implementation ("com.google.guava:guava:30.1-android")
    implementation(libs.recyclerview)

    // Test Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
