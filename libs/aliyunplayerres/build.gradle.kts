plugins {
    id ("com.android.library")
}

android {
    compileSdk=androids.compileSdkV
    buildToolsVersion=androids.buildToolsV

    defaultConfig {
        minSdk=androids.minSdkV
        targetSdk=androids.targetSdkV
//        versionCode = androids.vCode
//        versionName = androids.vName
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

}

dependencies {

    implementation(Depends.appcompat)
    implementation(Depends.constraintlayout)
    implementation(Depends.recyclerview)
    implementation(Depends.cardview)
    api("com.aliyun.sdk.android:AliyunPlayer:5.4.1-full")
    api(Depends.gson)
    //glide
    api(Depends.glide){
        exclude("androidx.customview","customview")
        exclude("androidx.fragment","fragment")
    }
}