plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.update"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    implementation(project(":libs:lib-log"))
    api(libs.androidx.core.ktx)
    implementation(libs.kotlinx.coroutines.android)
}
