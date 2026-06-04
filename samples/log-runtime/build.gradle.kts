plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
}


android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mic.log.runtime"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }


    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

}

//https://blog.csdn.net/lfq88/article/details/118222107
dependencies {
    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.databinding.runtime)
    api(project(mapOf("path" to ":libcore")))
}