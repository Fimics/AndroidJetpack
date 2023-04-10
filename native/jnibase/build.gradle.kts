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
    buildToolsVersion = "33.0.2"
    ndkVersion = "25.2.9519653"
}

dependencies {
    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation(Depends.appcompat)
    implementation(Depends.constraintlayout)
    implementation(Depends.vectordrawable)
    implementation(Depends.recyclerview)
    implementation(Depends.cardview)
    //navigation
    implementation(Depends.navigationfragment)
    implementation(Depends.navigation){
//        exclude("androidx.transition","transition")
        exclude(mapOf("group" to "androidx.transition","module" to "transition"))
    }
}