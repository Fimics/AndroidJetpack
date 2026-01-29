plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("kotlin-kapt")
    id("com.mic.autolog") // 应用我们的插桩插件
}


android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mic.apppy"

    defaultConfig {
        applicationId = "com.mic.apppy"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
    }


    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            // debug 默认开日志
        }

        getByName("release") {
            isMinifyEnabled = false
            // release 通常不插桩（上面插件已按 buildType=debug 限制）
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
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.databinding.runtime)
    api(project(":extras:libpy"))
    api(project(":log-runtime"))
}