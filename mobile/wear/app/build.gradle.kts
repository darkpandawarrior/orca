plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.github.takahirom.roborazzi")
}

android {
    namespace = "dev.orca.wear"
    compileSdk = 37

    defaultConfig {
        // Same id as the phone app: the Data Layer pairs watch and phone apps by applicationId
        // plus signing certificate.
        applicationId = "dev.orca.app"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    val wearCompose = "1.7.2"
    implementation("androidx.wear.compose:compose-material3:$wearCompose")
    implementation("androidx.wear.compose:compose-foundation:$wearCompose")
    implementation("androidx.activity:activity-compose:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")
    implementation("com.google.android.gms:play-services-wearable:20.0.1")

    // Tile + complication
    implementation("androidx.wear.protolayout:protolayout:1.4.2")
    implementation("androidx.wear.protolayout:protolayout-material3:1.4.2")
    implementation("androidx.wear.tiles:tiles:1.6.2")
    implementation("androidx.wear.watchface:watchface-complications-data-source-ktx:1.3.0")
    implementation("com.google.guava:guava:33.4.8-android")

    debugImplementation("androidx.compose.ui:ui-tooling:1.9.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.robolectric:robolectric:4.17")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.76.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.76.0")
    testImplementation("androidx.compose.ui:ui-test-junit4:1.9.0")
    testImplementation("androidx.test:core:1.7.0")
    testImplementation("androidx.test.ext:junit:1.3.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.9.0")
}
