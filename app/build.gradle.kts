import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization) // Lecture 5: id("kotlinx-serialization")
    alias(libs.plugins.ksp) // Lecture 7: annotation processing for Room
}

android {
    namespace = "com.yibopan.quizquest"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yibopan.quizquest"
        minSdk = 24 // Lecture 1: "Minimum SDK should be API 24"
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            // Lecture 9: minification/obfuscation can be switched on before release (Stage 10).
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        buildConfig = true // Lecture 9: BuildConfig.DEBUG turns logging off in release builds
    }

    testOptions {
        // Lets JVM unit tests call android.util.Log without crashing.
        unitTests.isReturnDefaultValues = true
    }

    // Lecture 7: export the Room schema so AutoMigration can compare database versions.
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_11)
    }
}

dependencies {
    // Android Studio template (Lecture 1 / Lecture 3)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Lecture 3: WindowSizeClass for tablets and foldables
    implementation(libs.androidx.adaptive)

    // Lecture 4: Koin dependency injection
    implementation(libs.bundles.koin)

    // Lecture 5: Retrofit, kotlinx.serialization, coroutines, OkHttp
    implementation(libs.bundles.networking)
    implementation(libs.compose.lifecycle) // collectAsStateWithLifecycle()
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Lecture 6: Jetpack Navigation for Compose
    implementation(libs.compose.navigation)

    // Lecture 7: Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Lecture 8: LeakCanary, debug builds only
    debugImplementation(libs.leakcanary.android)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    androidTestImplementation(libs.coroutines.test)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
