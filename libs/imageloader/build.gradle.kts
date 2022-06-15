plugins{
    id ("com.android.library")
    id ("org.jetbrains.kotlin.android")
}

android {
    compileSdk=androids.compileSdkVersion
    buildToolsVersion=androids.buildToolsVersion

    defaultConfig {
        minSdk=androids.minSdkVersion
        targetSdk=androids.targetSdkVersion
        versionCode = androids.versionCode
        versionName = androids.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    //依赖操作
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    val glideTransformationsVersion="4.3.0"

    implementation (fileTree(mapOf("dir" to "libs","include" to listOf("*.jar"))))
    Depends.impl(Depends.kotlins,::implementation)
    Depends.impl(Depends.glides,::implementation)
    //图片变换处理
    implementation ("jp.wasabeef:glide-transformations:$glideTransformationsVersion")
}
