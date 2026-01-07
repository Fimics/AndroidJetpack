plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    alias(libs.plugins.dagger.hilt)
}


android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mic.hilt"

    defaultConfig {
        applicationId = "com.mic.hilt"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
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

    //输出类型
    android.applicationVariants.all {
        //编译类型
        val buildType = this.buildType.name
        outputs.all {
            //输出apk
            if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                this.outputFileName = "Jetpack${defaultConfig.versionName}_$buildType.apk"
            }
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding =true

    }
}

//https://blog.csdn.net/lfq88/article/details/118222107
dependencies {
    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation(libs.androidx.appcompat)
    implementation(libs.gson)
    api(libs.okhttp)
    //hilt
    implementation(libs.dagger.hilt.android)
    kapt(libs.dagger.hilt.compiler)
    // 添加源码依赖（用于查看源码）
    compileOnly(libs.dagger.hilt.compiler)
//    implementation(libs.dagger.hilt.compiler)
//    implementation("androidx.hilt:hilt-lifecycle-viewmodel:1.0.0")
    implementation(libs.androidx.databinding.runtime)
    api(project(mapOf("path" to ":libcore")))
}