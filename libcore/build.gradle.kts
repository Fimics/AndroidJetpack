plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = androids.compileSdkV

    defaultConfig {
        minSdk = androids.minSdkV
        targetSdk=androids.targetSdkV
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    namespace = "com.mic.libcore"
}

dependencies {
    //kotlin
    api(Depends.kotlinstdlib)
    api(Depends.corektxo)
    api(Depends.kotlinxcoroutinesandroid)
    //okhttp
    api(Depends.okhttp)
    api(Depends.logginginterceptor)
    api(Depends.gson)

    //rxs
    api(Depends.rxandroid)
    api(Depends.rxjava)
    api(Depends.rxbinding2)

    //retrofit
    api(Depends.retrofit)
    api(Depends.convertergson)
    api(Depends.adapterrxjava)

    //glide
    api(Depends.glide){
        exclude("androidx.customview","customview")
        exclude("androidx.fragment","fragment")
    }

    //lifecycle
    api(Depends.viewmodel)
    api(Depends.livedata)
    api(Depends.lifecycle_runtime)
    api(Depends.lifecycle_viewmodel_savedstate)
    api(Depends.lifecycle_common_java8)
}