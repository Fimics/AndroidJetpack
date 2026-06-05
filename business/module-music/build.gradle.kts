import com.android.build.gradle.BaseExtension

// 组件化双模式开关（docs/01-arch.md §15.3）：
// runAlone.music=true 时本模块作为独立 App(application) 运行调试；否则作为 library 被壳工程集成。
val runAlone = (project.findProperty("runAlone.music") as String?)?.toBoolean() ?: false

if (runAlone) {
    apply(plugin = "com.android.application")
} else {
    apply(plugin = "com.android.library")
}
apply(plugin = "org.jetbrains.kotlin.android")

configure<BaseExtension> {
    namespace = "com.mic.guide.module.music"
    compileSdkVersion(libs.versions.compileSdk.get().toInt())

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        if (runAlone) {
            applicationId = "com.mic.guide.music.dev"
            targetSdk = libs.versions.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
        }
    }

    buildFeatures.viewBinding = true

    // 组件模式：把独立运行入口(src/runalone)并入编译；集成(library)模式下不参与，避免向宿主泄漏 LAUNCHER。
    if (runAlone) {
        sourceSets.getByName("main") {
            manifest.srcFile("src/runalone/AndroidManifest.xml")
            java.srcDir("src/runalone/java")
        }
    }
}

dependencies {
    // arch 以 api 暴露：业务页面继承 arch 的 Base 类
    "api"(project(":arch"))

    // 命令式 apply 后无类型化访问器，依赖配置名用字符串
    "implementation"(libs.androidx.fragment.ktx)            // by viewModels()
    "implementation"(libs.androidx.navigation.fragment.ktx) // nav 子图
    "implementation"(libs.androidx.recyclerview)
    "implementation"(project(":support:support-network"))   // MusicApiService + NetworkClient（真实网络）
    "implementation"(project(":api:api-music"))             // 实现并注册 MusicApi 能力（§6）
    "implementation"(project(":api:api-player"))            // 复用 support-media 的播放能力（经接口，§6.6）
}
