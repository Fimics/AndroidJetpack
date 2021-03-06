plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = 32

    defaultConfig {
        minSdk = 21
        targetSdk = 32

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    Depends.impl(Depends.okhttps,::implementation)
    Depends.impl(Depends.lifecycles,::implementation)
    Depends.impl(Depends.kotlins,::implementation)
    implementation(project(mapOf("path" to ":libs:imagemaster")))
    //异步组件 我们可以使用CallbackToFutureAdapter的getFuture函数将任意类型的回调转换成一个ListenableFuture实例，方便统一API的设计风格
    api("androidx.concurrent:concurrent-futures:1.0.0")
}