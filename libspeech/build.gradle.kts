plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.kk.speech"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a"))
            debugSymbolLevel = "FULL"
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.setSrcDirs(listOf("libs"))
        }
    }


    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
}

dependencies {
    // 导入jar包
    api(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    api(project(":library"))
//    api(project(":libunderpan"))
}