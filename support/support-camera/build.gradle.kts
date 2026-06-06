plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.camera"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    implementation(project(":libs:lib-log"))
    // CameraX：view（PreviewView + LifecycleCameraController 一站式）+ camera2 实现 + lifecycle 绑定
    api(libs.androidx.camera.view)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.kotlinx.coroutines.android)
}
