plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.media"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    // PlayerApi 接口（实现它）；不向消费方再暴露，故用 implementation
    implementation(project(":api:api-player"))
    // ApiRegistry + ComponentApplication 契约：support-media 作为「带组件的支撑模块」自注册能力
    implementation(project(":arch"))
    // Media3 / ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.common)
}