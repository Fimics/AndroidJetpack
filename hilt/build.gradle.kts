import org.jetbrains.kotlin.gradle.plugin.extraProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}


android {
    compileSdk=androids.compileSdkV
    buildToolsVersion=androids.buildToolsV

    defaultConfig {
        applicationId = "com.mic"
        minSdk=androids.minSdkV
        targetSdk=androids.targetSdkV
        versionCode = androids.vCode
        versionName = androids.vName
    }

    //签名
    signingConfigs{
        register("release"){
            keyAlias = "fimics"
            keyPassword ="123456"
            storeFile =file("../sign.jks")
            storePassword ="123456"
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
    kapt { generateStubs = true }
    // Allow references to generated code
    namespace = "com.mic"
}

//https://blog.csdn.net/lfq88/article/details/118222107
dependencies {
    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation(Depends.appcompat)
    implementation(Depends.gson)
    api(Depends.okhttp)
    //hilt
    api(Depends.hilt)
    kapt(Depends.htlt_compiler)

//    api(project(mapOf("path" to ":libcore")))
}