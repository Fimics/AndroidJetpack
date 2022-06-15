import BuildVersions.coroutines
import BuildVersions.datastore_version
import BuildVersions.gson_version
import BuildVersions.hilt_version
import BuildVersions.kotlin_version
import BuildVersions.lifecycle_version
import BuildVersions.nav_version
import BuildVersions.okhttp_version
import androids.corektxversion

//全局常量
object BuildVersions {
    const val gradle_version = "7.1.2"
//    const val kotlin_version = "1.5.21"
    const val kotlin_version = "1.6.10"

    const val nav_version = "2.4.1"
    const val okhttp_version = "4.9.0"
    const val gson_version = "2.8.7"
    const val hilt_version = "2.35.1"
    const val datastore_version = "1.0.0"
    const val lifecycle_version = "2.2.0"
    const val coroutines = "1.6.0"
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
    const val corektxversion="1.2.0"
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
    const val kotlinstdlib = "org.jetbrains.kotlin:kotlin-stdlib:${kotlin_version}"
    const val corektx = "androidx.core:core-ktx:${corektxversion}"
    const val kotlinxcoroutinesandroid =
        "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kotlin_version"


    //kotlins
    val kotlins = mapOf<String, String>(
        "kotlinstdlib" to "org.jetbrains.kotlin:kotlin-stdlib:${kotlin_version}",
        "corektx" to "androidx.core:core-ktx:${corektxversion}",
        "kotlinxcoroutinesandroid" to "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    )

    //rx
    val rxs = mapOf<String, String>(
        "rxjava" to "io.reactivex.rxjava2:rxjava:2.1.7",
        "rxandroid" to "io.reactivex.rxjava2:rxandroid:2.0.1",
        "adapterrxjava" to "com.squareup.retrofit2:adapter-rxjava2:2.5.0",
        "rxbinding2" to "com.jakewharton.rxbinding2:rxbinding:2.0.0",
        "rxpermissions2" to "com.tbruyelle.rxpermissions2:rxpermissions:0.9.4@aar",
    )

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

    //test
    val tests = mapOf<String, String>(
        "testrunner" to "androidx.test.runner.AndroidJUnitRunner",
        "junit" to "junit:junit:4.12",
        "junitandroidx" to "androidx.test.ext:junit:1.1.2",
        "espresso" to "androidx.test.espresso:espresso-core:3.3.0",
    )

    //navigation
    val navigations = mapOf<String, String>(
        "navigationfragment" to "androidx.navigation:navigation-fragment:$nav_version",
        "navigation" to "androidx.navigation:navigation-ui:$nav_version",
    )

    //okhttp
    val okhttps = mapOf<String, String>(
        "okhttp" to "com.squareup.okhttp3:okhttp:$okhttp_version",
        "logginginterceptor" to "com.squareup.okhttp3:logging-interceptor:+",
        "gson" to "com.google.code.gson:gson:$gson_version",
    )

    //retrofit
    val retrofits = mapOf<String, String>(
        "retrofit" to "com.squareup.retrofit2:retrofit:2.8.1",
        "convertergson" to "com.squareup.retrofit2:converter-gson:2.8.1",
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
//        "glidetransformations" to "jp.wasabeef:glide-transformations:4.0.0",
//        "glidecompiler" to "com.github.bumptech.glide:compiler:4.9.0",
    )

    //datastore
    val datastores = mapOf<String, String>(
        "datastore-core" to "androidx.datastore:datastore-core:$datastore_version",
        "datastore-preferences-core" to "androidx.datastore:datastore-preferences-core:$datastore_version"
    )

    //hilt
    val hilts = mapOf<String, String>(
        "hilt" to "com.google.dagger:hilt-android:$hilt_version",
        "htlt-compiler" to "com.google.dagger:hilt-android-compiler:$hilt_version"
    )

    //lifecycle
    val lifecycles = mapOf<String, String>(
        // ViewModel
        "viewmodel" to "androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version",
        // LiveData
        "livedata" to "androidx.lifecycle:lifecycle-livedata:$lifecycle_version",
        // Lifecycles only (without ViewModel or LiveData)
        "lifecycle-runtime" to "androidx.lifecycle:lifecycle-runtime:$lifecycle_version",
        // Saved state module for ViewModel
        "lifecycle-viewmodel-savedstate" to "androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version",
        // Annotation processor
//        "lifecycle-compiler" to "androidx.lifecycle:lifecycle-compiler:$lifecycle_version",
        // alternately - if using Java8, use the following instead of lifecycle-compiler
        "lifecycle-common-java8" to "androidx.lifecycle:lifecycle-common-java8:$lifecycle_version",
        // optional - helpers for implementing LifecycleOwner in a Service
        "lifecycle-service" to "androidx.lifecycle:lifecycle-service:$lifecycle_version",
        // optional - ProcessLifecycleOwner provides a lifecycle for the whole application process
        "lifecycle-process" to "androidx.lifecycle:lifecycle-process:$lifecycle_version"

    )


    //kapt TOOD
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

