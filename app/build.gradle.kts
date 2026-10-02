plugins {
    id("com.android.application")
}

android {
    namespace = "com.abeyytechxy.cnguard"
    compileSdk {
        version = release(37) {
            minorApiLevel = 2
        }
    }

    defaultConfig {
        applicationId = "com.abeyytechxy.cnguard"
        minSdk = 31
        targetSdk = 36
        versionCode = 2
        versionName = "0.2.0-alpha01"
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

    packaging {
        resources {
            pickFirsts += setOf(
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.md"
            )
        }
    }
}

dependencies {
    implementation("com.flyfishxu:kadb-android:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")

    // Kadb 2.1.4 declares Bouncy Castle 1.84. Pin a patched release.
    implementation("org.bouncycastle:bcprov-jdk18on:1.86")
    implementation("org.bouncycastle:bcpkix-jdk18on:1.86")

    testImplementation("junit:junit:4.13.2")
}
