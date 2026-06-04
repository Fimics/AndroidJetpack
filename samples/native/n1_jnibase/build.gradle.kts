plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 33
    buildToolsVersion = "30.0.3"
    namespace = "com.mic.jnibase"

    defaultConfig {
        applicationId = "com.mic.jnibase"
        minSdk = 30
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // TODO 第四步
        externalNativeBuild {
            cmake {
                // cppFlags = "" // 默认五大平台

                // 指定CPU架构，Cmake的本地库， 例如：native-lib ---> armeabi-v7a
                abiFilters += listOf("arm64-v8a")
            }
        }

        // TODO 第五步
        // 指定CPU架构，打入APK lib/CPU平台
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    // sourceSet 可以修改 jniLibs 目录  修改成 aaa
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.2")
    implementation("androidx.core:core-ktx:1.8.0")
    testImplementation("junit:junit:4.12")
    androidTestImplementation("androidx.test.ext:junit:1.1.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.3.0")

    // TODO 第六步：主要是 有没有提供 java的 jar包
    // implementation(files("libs\\fmod.jar"))
}