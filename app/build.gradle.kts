import java.util.Properties

// APKs are renamed to fuel-v<version>.apk by the GitHub Actions workflows rather
// than by the deprecated variant output API, which does not play well with the
// Gradle configuration cache.
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

/**
 * Release signing is optional so that a fresh clone can always build.
 *
 * Supply either:
 *  - a `keystore.properties` file in the repository root (never committed), or
 *  - the environment variables used by the release GitHub Actions workflow.
 *
 * When neither is present the release variant falls back to the debug signing
 * identity, which is fine for local experiments but must not be used for the
 * APKs actually installed on the phone (see README "APK signing").
 */
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { load(it) }
    }
}

fun releaseSigningValue(propertyKey: String, envKey: String): String? =
    keystoreProperties.getProperty(propertyKey) ?: System.getenv(envKey)

val releaseStoreFilePath = releaseSigningValue("storeFile", "FUEL_KEYSTORE_FILE")
val hasReleaseSigning = releaseStoreFilePath != null && file(releaseStoreFilePath).exists()

android {
    namespace = "com.personal.fuel"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.personal.fuel"
        minSdk = 30
        targetSdk = 35
        versionCode = 6
        versionName = "1.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        resourceConfigurations += listOf("en")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = releaseSigningValue("storePassword", "FUEL_KEYSTORE_PASSWORD")
                keyAlias = releaseSigningValue("keyAlias", "FUEL_KEY_ALIAS")
                keyPassword = releaseSigningValue("keyPassword", "FUEL_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // No applicationIdSuffix, and the same signing identity as release:
            // there is one Fuel app on the phone, and any APK updates any other
            // without an uninstall. An uninstall is what loses the log.
            versionNameSuffix = "-debug"
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                // Local builds only. Android's generated debug key differs from
                // machine to machine, so an APK signed with it cannot update an
                // install signed by any other machine — which is exactly what
                // ephemeral CI runners produce.
                signingConfigs.getByName("debug")
            }
        }
        release {
            // Kept off deliberately: this is a personal sideloaded app where a
            // silently broken release build costs far more than a few MB of APK.
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = if (hasReleaseSigning) {
                signingConfigs.getByName("release")
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.generateKotlin", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.adaptive)
    implementation(libs.androidx.window)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    implementation(libs.androidx.work.runtime.ktx)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    // Real org.json so the backup format can be tested off-device.
    testImplementation(libs.json)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
