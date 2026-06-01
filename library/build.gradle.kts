plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk=libs.versions.compileSdk.get().toInt()
    buildToolsVersion=libs.versions.buildVersion.get()

    defaultConfig {
        minSdk=libs.versions.minSdk.get().toInt()
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
    }

    namespace = "com.noetix.libcore"
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar", "*.jar"))))
//    XDepends.implGroup(XDepends.views,::implementation)
//    implementation(XDepends.ktxcore)
//    implementation(XDepends.kotlinxcoroutinesandroid)
    implementation(libs.kotlinxcoroutines)

    //lifecycle view model
    implementation(libs.viewmodel)
    implementation(libs.lifecycle)

    //rx
    implementation(libs.rxjava)
    implementation(libs.rxandroid)

    //okhttp
    api(libs.okhttp)
    api(libs.logginginterceptor)

    //gson
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.convertergson)
    implementation(libs.adapterrxjava)

    api(libs.serialport)
    api(libs.rxjava2)
    api(libs.rxandroid2)
}
