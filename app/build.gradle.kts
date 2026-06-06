plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.jetbrains.kotlin.compose)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mic.guide"
    defaultConfig {
        applicationId = "com.mic.guide"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        multiDexEnabled = true
        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a"))
        }
    }


    buildTypes {

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
        }
    }


    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlin.compiler.extension.get()
    }

    // 正确的 sourceSets 配置 - 使用 Kotlin DSL 语法
    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
            assets.srcDirs("src/main/assets")
        }
    }
}

dependencies {
    implementation(fileTree(mapOf("includes" to listOf("*.aar", "*.jar"), "dir" to "libs")))

    // 架构层 + 业务模块（组件化：集成模式下作为 library 被壳工程集成，见 §15）
    implementation(project(":arch"))
    implementation(project(":support:support-router"))      // 注册 AppNavigator 门面
    implementation(libs.androidx.navigation.fragment.ktx)   // 壳工程承载 NavHost + 汇总子图
    implementation(libs.androidx.navigation.ui.ktx)         // BottomNavigationView.setupWithNavController
    implementation(project(":business:module-home"))
    implementation(project(":business:module-chat"))
    implementation(project(":business:module-music"))
    implementation(project(":business:module-video"))
    implementation(project(":business:module-settings"))
    implementation(project(":support:support-media"))      // PlayerApi 实现（Media3），SPI 自注册供 music/video 复用

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.composeui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.ext)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    implementation(libs.androidx.multidex)
    implementation(libs.gson)
    implementation(libs.gson)
    implementation(libs.commons.csv)
    implementation(libs.live.event.bus)
    implementation(libs.kotlinx.serialization.json)
    api(libs.okhttp)
    api(libs.logging.interceptor)
    api(libs.androidx.paging.runtime)
    api(libs.androidx.work.runtime)
    implementation(libs.androidx.databinding.runtime)

}
