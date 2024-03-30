plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.fitzonetrainer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.fitzonetrainer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    //image circle
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation ("com.github.Drjacky:ImagePicker:2.3.22")
    //graph chart
    implementation ("com.github.PhilJay:MPAndroidChart:v3.1.0")
    //BMI
    implementation ("com.github.Gruzer:simple-gauge-android:0.3.1")

    //shimmer
    implementation ("com.facebook.shimmer:shimmer:0.5.0")
    implementation ("com.todkars:shimmer-recyclerview:0.4.1")

    //searchView
    implementation ("com.github.mancj:MaterialSearchBar:0.8.5")

    // Import the Firebase BoM
    implementation(platform("com.google.firebase:firebase-bom:32.7.4"))

    // Glide
    implementation ("com.github.bumptech.glide:glide:4.12.0")

    // TODO: Add the dependencies for Firebase products you want to use
    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")


    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.google.firebase:firebase-firestore:24.10.3")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("androidx.activity:activity:1.8.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}