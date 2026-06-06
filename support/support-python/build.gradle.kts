plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    // Chaquopy 须在 Android 插件之后应用；全工程仅此一个模块可应用（多模块会冲突）
    id("com.chaquo.python")
}

android {
    namespace = "com.mic.guide.support.python"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        // Chaquopy 打包对应 ABI 的 CPython 运行时
        ndk {
            abiFilters += listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
        }
    }
}

chaquopy {
    defaultConfig {
        version = "3.11"
        // 如需第三方包：pip { install("numpy") }
    }
}

dependencies {
    implementation(project(":libs:lib-log"))
    implementation(libs.kotlinx.coroutines.android)
}
