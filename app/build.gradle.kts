plugins {
    id("com.android.application")
}

android {
    namespace = "com.abeyytechxy.cnguard"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.abeyytechxy.cnguard"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0-alpha01"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
}
