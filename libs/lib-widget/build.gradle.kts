plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.lib.widget"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { viewBinding = true }
}

dependencies {
    // lib-ui 管主题/令牌，lib-widget 管控件（§2 树注）
    implementation(project(":libs:lib-ui"))
    api(libs.androidx.appcompat)
    api(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
}
