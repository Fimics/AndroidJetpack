
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
//    id("kotlin-kapt")
//    id("dagger.hilt.android.plugin")
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
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

//    //依赖操作
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//    kotlinOptions {
//        jvmTarget = "1.8"
//    }
    buildFeatures {
        viewBinding = true
    }
}



dependencies {
    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    implementation("androidx.appcompat:appcompat:1.4.1")
    implementation("com.google.android.material:material:1.4.+")
    implementation("androidx.constraintlayout:constraintlayout:2.1.3")
    Depends.impl(Depends.views,::implementation)
    Depends.impl(Depends.navigations,::implementation)
    Depends.impl(Depends.glides,::implementation)
    Depends.impl(Depends.okhttps,::implementation)
    Depends.impl(Depends.lifecycles,::implementation)
    Depends.impl(Depends.kotlins,::implementation)
    Depends.impl(Depends.retrofits,::implementation)
    Depends.impl(Depends.rxs,::implementation)
    api(project(mapOf("path" to ":libcore")))
}