plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.arch"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        // 供 Base 类的泛型 ViewBinding 使用
        viewBinding = true
    }
}

dependencies {
    // 以 api 暴露给业务模块：业务用 `api(project(":arch"))` 即可继承以下基础能力
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.material)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment)

    // 生命周期 / 协程
    api(libs.androidx.lifecycle.runtime.ktx)
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata)
    api(libs.kotlinx.coroutines.android)

    testImplementation(libs.junit)
}
