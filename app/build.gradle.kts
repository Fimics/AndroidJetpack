plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
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

    buildFeatures {
        viewBinding = true
    }
    namespace = "com.mic"
}



dependencies {
    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    //views
    implementation(Depends.appcompat)
    implementation(Depends.constraint)
    implementation(Depends.vectordrawable)
    implementation(Depends.recyclerview)
    implementation(Depends.cardview)
    implementation(Depends.material)

    //navigation
    implementation(Depends.navigationfragment)
    implementation(Depends.navigation)

    api(project(mapOf("path" to ":libcore")))
}