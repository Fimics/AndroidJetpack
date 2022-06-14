//全局常量
object BuildVersions {
    const val gradle_version = "7.1.2"
    const val kotlin_version = "1.5.21"
}

//应用配置
object androids {
    const val compileSdkVersion = 32
    const val buildToolsVersion = "30.0.3"
    const val applicationId = "com.mic"
    const val minSdkVersion = 21
    const val targetSdkVersion = 29
    const val versionCode = 1
    const val versionName = "1.0"
}


//依赖配置
object Depends {

    /**
     * 分组导入
     */

    //Kotlin核心库
//    const val ktx_core = "androidx.core:core-ktx:1.2.0"

    const val multidex = "com.android.support:multidex:1.1.0"
    const val workmanager = "androidx.work:work-runtime:2.2.0"
    const val kotlinstdlib = "org.jetbrains.kotlin:kotlin-stdlib:${BuildVersions.kotlin_version}"
    const val corektx = "androidx.core:core-ktx:${BuildVersions.kotlin_version}"

    //rx
//    val rxs = mapOf<String, String>(
//        "rxjava" to "io.reactivex.rxjava2:rxjava:2.1.7",
//        "rxandroid" to "io.reactivex.rxjava2:rxandroid:2.0.1",
//        "adapterrxjava" to "com.squareup.retrofit2:adapter-rxjava2:2.5.0",
//        "rxbinding2" to "com.jakewharton.rxbinding2:rxbinding:2.0.0",
//        "rxpermissions2" to "com.tbruyelle.rxpermissions2:rxpermissions:0.9.4@aar",
//    )

    //view
    val views = mapOf<String, String>(
        "appcompat" to "androidx.appcompat:appcompat:1.3.0",
        "constraint" to "androidx.constraintlayout:constraintlayout:2.0.4",
        "vectordrawable" to "androidx.vectordrawable:vectordrawable:1.0.1",
        "legacy" to "androidx.legacy:legacy-support-v4:1.0.0",
        "recyclerview" to "androidx.recyclerview:recyclerview:1.0.0",
        "cardview" to "androidx.cardview:cardview:1.0.0",
        "material" to "com.google.android.material:material:1.4.0",
    )

    //retrofit
    val retrofits = mapOf<String, String>(
        "retrofit" to "com.squareup.retrofit2:retrofit:2.8.1",
        "convertergson" to "com.squareup.retrofit2:converter-gson:2.8.1",
        "gson" to "com.google.code.gson:gson:2.6.2",
    )

    //test
    val tests = mapOf<String, String>(
        "testrunner" to "androidx.test.runner.AndroidJUnitRunner",
        "junit" to "junit:junit:4.12",
        "junitandroidx" to "androidx.test.ext:junit:1.1.2",
        "espresso" to "androidx.test.espresso:espresso-core:3.3.0",
    )

    //navigation
    val navigations = mapOf<String, String>(
        "navigationfragment" to "androidx.navigation:navigation-fragment:2.0.0",
        "navigation" to "androidx.navigation:navigation-ui:2.0.0",
    )

    //okhttp
    val okhttps = mapOf<String, String>(
        "okhttp" to "com.squareup.okhttp3:okhttp:4.2.0",
        "logginginterceptor" to "com.squareup.okhttp3:logging-interceptor:+",
    )

    //room数据库
    val rooms = mapOf<String, String>(
        "roomruntime" to "android.arch.persistence.room:runtime:1.1.1",
        "lifecycleextensions" to "android.arch.lifecycle:extensions:1.1.1",
        "roomcompiler" to "android.arch.persistence.room:compiler:1.1.1",
        "lifecyclecompiler" to "android.arch.lifecycle:compiler:1.1.1",
    )


    //图片加载
    val glides = mapOf<String, String>(
        "glide" to "com.github.bumptech.glide:glide:4.9.0",
        "glidetransformations" to "jp.wasabeef:glide-transformations:4.0.0",
        "glidecompiler" to "com.github.bumptech.glide:compiler:4.9.0",
        "photoView" to "com.github.chrisbanes:PhotoView:2.3.0@aar"
    )


    fun impl(map: Map<String, String>, block: (String) -> Unit) {
        map.forEach { (key, value) ->
            println("key ->$key  value-> $value")
            block(value)
        }
    }

    fun impl(component: String, block: (String) -> Unit) {
        println("component ->$component")
        block(component)
    }

}

