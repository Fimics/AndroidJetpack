import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    namespace = "com.mic.jnibase"
    compileSdk = androids.compileSdkV

    defaultConfig {
        compileSdk=androids.compileSdkV
        buildToolsVersion=androids.buildToolsV

        defaultConfig {
            applicationId = "com.mic.jnibase"
            minSdk=androids.minSdkV
            targetSdk=androids.targetSdkV
            versionCode = androids.vCode
            versionName = androids.vName
        }

        externalNativeBuild {
            cmake {
                cppFlags("")
            }
        }
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
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(Depends.appcompat)
    implementation(Depends.material)
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.4.+")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
}