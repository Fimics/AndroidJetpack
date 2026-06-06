plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.ai"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
    buildFeatures { buildConfig = true }
}

dependencies {
    // ApiRegistry + ComponentApplication 契约：support-ai 作为「带组件的支撑模块」自注册 AiChatClient（§6 / §15.5）
    implementation(project(":arch"))
    // 日志门面
    implementation(project(":libs:lib-log"))

    // 网络：非流式走 Retrofit；流式（SSE）走裸 OkHttp，逐 token 回调
    api(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)

    // Flow / 协程：chatStream 用 callbackFlow 把 SSE 包成 Flow<String>
    implementation(libs.kotlinx.coroutines.android)
}