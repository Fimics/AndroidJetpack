// 性能基线模块（Macrobenchmark）：生成 Baseline Profile + 启动/滚动基准（§2 树 ➕）。
// 这是一个 com.android.test 模块，针对 :app 跑插桩基准，需要连接设备/受管模拟器才能实际执行。
plugins {
    alias(libs.plugins.android.test)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.mic.guide.baselineprofile"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = 28 // Macrobenchmark 要求 minSdk ≥ 28
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // 被测应用工程
    targetProjectPath = ":app"
}

// 受管虚拟设备（CI 上可无设备直接 `./gradlew :app:generateBaselineProfile`）
baselineProfile {
    useConnectedDevices = true
}

dependencies {
    implementation(libs.androidx.junit.ext)
    implementation(libs.androidx.espresso.core)
    implementation(libs.androidx.test.uiautomator)
    implementation(libs.androidx.benchmark.macro.junit4)
}
