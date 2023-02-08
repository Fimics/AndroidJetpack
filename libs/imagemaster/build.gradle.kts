
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk=androids.compileSdkV
    buildToolsVersion=androids.buildToolsV

    defaultConfig {
        minSdk=androids.minSdkV
        targetSdk=androids.targetSdkV
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    namespace = "com.lawaken.image"

}

dependencies {

}