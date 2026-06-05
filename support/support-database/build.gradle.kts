plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)   // Room 注解处理
}

android {
    namespace = "com.mic.guide.support.database"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    // Room 运行时 + KTX（suspend / Flow 支持），编译期注解处理用 kapt
    api(libs.androidx.room.runtime)
    api(libs.androidx.room.ktx)
    kapt(libs.androidx.room.compiler)
    implementation(libs.kotlinx.coroutines.android)   // Dao 的 Flow 返回类型
}