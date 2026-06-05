import com.android.build.gradle.BaseExtension

// 组件化双模式开关（docs/01-arch.md §15.3）：
// runAlone.chat=true 时本模块作为独立 App(application) 运行调试；否则作为 library 被壳工程集成。
val runAlone = (project.findProperty("runAlone.chat") as String?)?.toBoolean() ?: false

if (runAlone) {
    apply(plugin = "com.android.application")
} else {
    apply(plugin = "com.android.library")
}
apply(plugin = "org.jetbrains.kotlin.android")

configure<BaseExtension> {
    namespace = "com.mic.guide.module.chat"
    compileSdkVersion(libs.versions.compileSdk.get().toInt())

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        if (runAlone) {
            applicationId = "com.mic.guide.chat.dev"
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
}
