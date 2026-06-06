plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.lib.log"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    // KLog（迁移自 libcore）：xlog 后端 + gson 美化 JSON + AppGlobals 取全局 Application
    implementation(project(":libs:lib-common"))
    api(libs.xlog)
    implementation(libs.gson)
}