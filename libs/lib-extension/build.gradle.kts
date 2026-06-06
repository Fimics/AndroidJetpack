plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.lib.extension"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    api(libs.androidx.core.ktx)
    // ToastUtils 依赖全局 Application（lib-common），日志走 lib-log
    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-log"))
}