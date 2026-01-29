plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
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

    namespace = "com.mic.libc"

}

dependencies {
    //kotlin
    api(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.android)

    //kotlin
    api(libs.androidx.core.ktx)
    api(libs.kotlinx.coroutines.android)

    //navigation
    api(libs.androidx.navigation.fragment)
    api(libs.androidx.navigation.ui)
    //okhttp
    api(libs.okhttp)
    api(libs.logging.interceptor)
    api(libs.gson)
    //rxs
    api(libs.rx3.android)
    api(libs.rx3.java)
    api(libs.rx2.binding)

    //retrofit
    api(libs.retrofit2.retrofit)
    api(libs.retrofit2.converter.gson)
    api(libs.retrofit2.adapter.rxjava3)

    //datastore
    api(libs.androidx.datastore.preferences)
    api(libs.androidx.datastore.preferences.core)

    //room
    api(libs.androidx.room.runtime)

    //glide
    api(libs.glide){
        exclude("androidx.customview","customview")
        exclude("androidx.fragment","fragment")
    }
    //lifecycle
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.lifecycle.livedata)
    api(libs.androidx.lifecycle.runtime)
    api(libs.androidx.lifecycle.viewmodel.savedstate)
    api(libs.androidx.lifecycle.common.java8)
    api(libs.androidx.lifecycle.process )
    api(libs.androidx.lifecycle.service)

    api(libs.xlog)

}