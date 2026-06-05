plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.router"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
}

dependencies {
    // 路由门面封装 Jetpack Navigation：只需 NavController（navigation-runtime）
    api(libs.androidx.navigation.runtime.ktx)
}
