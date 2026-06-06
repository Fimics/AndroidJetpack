// 公共资源模块：集中放跨模块复用的 colors / strings / dimens / theme（§2 树 ➕）。
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.common"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    // 资源型模块，向使用方传递 Material 主题父样式
    api(libs.androidx.appcompat)
    api(libs.androidx.material)
}
