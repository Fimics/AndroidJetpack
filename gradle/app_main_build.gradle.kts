import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.noetix.robotics"

    // 添加这里：启用 BuildConfig
    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "com.noetix.robotics"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        multiDexEnabled = true
        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a"))
        }

        // 获取三个模块的 Commit 信息
        val mainCommit = getGitCommit(".")
        val libNoetixCommit = getGitCommit("android_libnoetix")
        val cppCommit = getGitCommit("android_libnoetix/libNoetix/src/main/cpp")
        val date = SimpleDateFormat("yyyyMMddHHmm").format(Date())

        // 拼接 Commit 信息
        val combinedCommits = "${date}:${mainCommit.take(7)}:${libNoetixCommit.take(7)}:${cppCommit.take(7)}"

        // 写入 BuildConfig（需要先启用 buildFeatures.buildConfig）
        buildConfigField("String", "VERSION_COMMITS", "\"$combinedCommits\"")

        // 写入资源文件
        resValue("string", "version_commits", combinedCommits)

        // 打印调试信息
        println("=== Git Commit Information ===")
        println("Main Project Commit: ${mainCommit.take(7)}")
        println("android_libnoetix Commit: ${libNoetixCommit.take(7)}")
        println("cpp Module Commit: ${cppCommit.take(7)}")
        println("Combined Commits: $combinedCommits")
    }

    signingConfigs {
        create("keyStore") {
            storeFile = file("../app_main/platform.jks")
            keyPassword = "android"
            keyAlias = "androidplatformkey"
            storePassword = "android"
        }
    }

    buildTypes {
        val signConfig = signingConfigs.getByName("keyStore")

        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signConfig
        }

        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signConfig
        }
    }

    // 输出类型
    android.applicationVariants.all {
        // 编译类型
        val buildType = this.buildType.name
        println("buildType name -> $buildType")

        // 获取三个模块的短 Commit ID
        val mainCommit = getGitCommit(".").take(7)
        val libNoetixCommit = getGitCommit("android_libnoetix").take(7)
        val cppCommit = getGitCommit("android_libnoetix/libNoetix/src/main/cpp").take(7)

        var vName = this.versionName
        val date = SimpleDateFormat("yyyyMMddHHmm").format(Date())
        outputs.all {
            // 判断是否是输出 apk 类型
            if (this is ApkVariantOutputImpl) {
                // 修改 APK 命名规则，包含 Commit 信息
                this.outputFileName = "${flavorName}_${vName}_${date}_${mainCommit}_${libNoetixCommit}_${cppCommit}.apk"
            }
        }
    }

    flavorDimensions += listOf("terminalType")
    productFlavors {
        create("main_bionic") {
        }
    }

    packagingOptions {
        pickFirst("lib/arm64-v8a/libc++_shared.so")
        // 如果需要，也可以为其他ABI添加
        pickFirst("lib/armeabi-v7a/libc++_shared.so")
        pickFirst("lib/x86/libc++_shared.so")
        pickFirst("lib/x86_64/libc++_shared.so")
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs("libs")
        }
    }
    // 注意：这里移除重复的 buildFeatures 配置
    // buildFeatures {
    //     viewBinding = true
    // }
}

dependencies {
    implementation(fileTree(mapOf("includes" to listOf("*.aar", "*.jar"), "dir" to "libs")))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Speech Engine
    implementation("androidx.multidex:multidex:2.0.1")
    implementation("com.bytedance.speechengine:speechengine_tts_tob:5.4.8")

    implementation("com.alibaba:fastjson:1.1.72.android")
    implementation(project(":libCore"))
    implementation(project(":libUpgrade"))
    implementation(project(":libMMSP"))
    implementation(project(":libAiui"))
    implementation(project(":android_libnoetix:libNoetix"))
}

// 获取 Git Commit 的函数
fun getGitCommit(submodulePath: String = "."): String {
    return try {
        val process = ProcessBuilder("git", "-C", submodulePath, "rev-parse", "HEAD")
            .directory(project.rootDir)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        val output = process.inputStream.bufferedReader().readText().trim()
        val error = process.errorStream.bufferedReader().readText()
        process.waitFor()

        if (process.exitValue() != 0) {
            println("Git command failed for path '$submodulePath': $error")
            "unknown"
        } else if (output.isNotEmpty()) {
            output
        } else {
            "unknown"
        }
    } catch (e: Exception) {
        println("Error getting Git commit for path '$submodulePath': ${e.message}")
        "unknown"
    }
}

// 可选：创建生成版本信息的任务
tasks.register("printVersionInfo") {
    doLast {
        println("\n=== Detailed Version Information ===")

        val mainCommit = getGitCommit(".")
        val libNoetixCommit = getGitCommit("android_libnoetix")
        val cppCommit = getGitCommit("android_libnoetix/libNoetix")

        println("Main Project:")
        println("  Full Commit: $mainCommit")
        println("  Short Commit: ${mainCommit.take(7)}")

        println("\nandroid_libnoetix:")
        println("  Full Commit: $libNoetixCommit")
        println("  Short Commit: ${libNoetixCommit.take(7)}")

        println("\ncpp Module:")
        println("  Full Commit: $cppCommit")
        println("  Short Commit: ${cppCommit.take(7)}")

        val combinedCommits = "main:${mainCommit.take(7)}, lib:${libNoetixCommit.take(7)}, cpp:${cppCommit.take(7)}"
        println("\nCombined: $combinedCommits")
    }
}