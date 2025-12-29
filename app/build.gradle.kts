plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"  // 修正版本号
    id("org.jetbrains.kotlin.plugin.compose")
    id("kotlin-kapt")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.mic"
    defaultConfig {
        applicationId = "com.mic"
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

    // 对于 Kotlin 项目，还需要添加
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
        dataBinding = true
        viewBinding = true
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3" // 根据你的 Compose 版本调整
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
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-csv:1.9.0")
    api("io.github.jeremyliao:live-event-bus-x:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    api(libs.okhttp)
    api(libs.logging.interceptor)
    api(libs.androidx.paging.runtime)
    api(libs.androidx.work.runtime)
    api(libs.androidx.databinding.runtime)
//    api(libs.androidx.databinding.common)

    api(project(":libcore"))

}
