plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.storage"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    api(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.coroutines.android)
    // Prefs（SharedPreferences 门面，迁移自 libcore P）依赖全局 Application
    implementation(project(":libs:lib-common"))
}