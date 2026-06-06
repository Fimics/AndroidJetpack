plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.permission"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    api(libs.androidx.activity.ktx)
    // PermissionUtils（迁移自 libcore）静态检查依赖全局 Application
    implementation(project(":libs:lib-common"))
    implementation(libs.androidx.core.ktx)
}