plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.10"  // 修正版本号
    id("com.google.protobuf")
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    namespace = "com.grpc.pb"
    defaultConfig {
        applicationId = "com.grpc.pb"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()
        multiDexEnabled = true
        ndk {
            abiFilters.addAll(arrayOf("arm64-v8a"))
        }
    }

    signingConfigs {
        create("keyStore") {
            storeFile = file("../app/platform.jks")
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
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    implementation("androidx.multidex:multidex:2.0.1")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-csv:1.9.0")
    api("io.github.jeremyliao:live-event-bus-x:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.github.tonyofrancis.Fetch:fetch2:3.4.1")
    api(libs.okhttp)
    api(libs.logginginterceptor)

    // gRPC 依赖 - 使用正确的 Kotlin DSL 语法
    implementation("io.grpc:grpc-okhttp:1.61.0")
    implementation("io.grpc:grpc-protobuf-lite:1.61.0")
    implementation("io.grpc:grpc-stub:1.61.0")
    implementation("com.google.protobuf:protobuf-javalite:3.25.1")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }

    plugins {
        create("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.61.0"
        }
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") {
                    option("lite")
                }
            }
            task.plugins {
                create("grpc") {
                    option("lite")
                }
            }
        }
    }
}