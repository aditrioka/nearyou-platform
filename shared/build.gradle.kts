import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "2.2.20"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    iosArm64()
    iosSimulatorArm64()
    
    jvm()
    
    sourceSets {
        commonMain.dependencies {
            // Serialization
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

            // DateTime
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

            // Coroutines
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

            // Ktor Client
            implementation("io.ktor:ktor-client-core:3.3.0")
            implementation("io.ktor:ktor-client-content-negotiation:3.3.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.0")
            implementation("io.ktor:ktor-client-logging:3.3.0")

            // Koin for Dependency Injection
            implementation(libs.koin.core)
        }
        androidMain.dependencies {
            // Ktor Client Engine for Android
            implementation("io.ktor:ktor-client-okhttp:3.3.0")

            // Encrypted SharedPreferences for secure token storage
            implementation("androidx.security:security-crypto:1.1.0-alpha06")

            // Koin for Android
            implementation(libs.koin.android)
        }
        iosMain.dependencies {
            // Ktor Client Engine for iOS
            implementation("io.ktor:ktor-client-darwin:3.3.0")
        }
        jvmMain.dependencies {
            // Ktor Client Engine for JVM
            implementation("io.ktor:ktor-client-cio:3.3.0")

            // SLF4J for logging
            implementation("org.slf4j:slf4j-api:2.0.9")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
            implementation("io.ktor:ktor-client-mock:3.3.0")
        }
    }
}

android {
    namespace = "id.nearyou.app.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
