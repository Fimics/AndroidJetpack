plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.chaquo.python")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        ndk {
            // 真机 + 模拟器常用组合：
            abiFilters += setOf("arm64-v8a", "armeabi-v7a", "x86_64")
            // 如果你只跑真机（绝大多数现在都是 arm64）：
            // abiFilters += setOf("arm64-v8a")
        }
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

    namespace = "com.mic.libpy"

}
chaquopy {
    defaultConfig {
        // 如果你需要 pip 依赖，放这里
        pip {
            install("requests")
        }
        
        // 配置Python标准库
//        buildPython {
//            // 使用系统Python环境
//            buildPython("python3")
//        }
        // 可选：buildPython(...) 等也都在这里配
    }

    // 如果你要自定义 python 源码目录，用 sourceSets
    sourceSets {
        getByName("main") {
            srcDir("src/main/python")
        }
    }
}
