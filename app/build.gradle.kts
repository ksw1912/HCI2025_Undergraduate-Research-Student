plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.project.googlespeechtotext"
    compileSdk = 35

    packagingOptions {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    defaultConfig {
        applicationId = "com.project.googlespeechtotext"
        minSdk = 24
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
    // CameraX 라이브러리
    implementation("androidx.camera:camera-core:1.3.0")
    implementation("androidx.camera:camera-lifecycle:1.3.0")
    implementation("androidx.camera:camera-view:1.3.0")
    implementation("androidx.camera:camera-extensions:1.3.0")

    // MediaPipe 라이브러리
    implementation("com.google.mediapipe:tasks-vision:0.20230731") {
        exclude(group = "com.google.protobuf", module = "protobuf-javalite")
    }

    implementation("com.google.protobuf:protobuf-java:3.25.5")
//    implementation("com.google.protobuf:protobuf-javalite:3.25.5")

    implementation("com.google.cloud:google-cloud-speech:4.51.0")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.32.1")
    implementation("io.grpc:grpc-okhttp:1.70.0")
    implementation("io.grpc:grpc-stub:1.70.0")

    implementation("com.google.api:gax:2.61.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}