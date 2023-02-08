import BuildVersions.coroutines
import BuildVersions.datastore_version
import BuildVersions.gson_version
import BuildVersions.hilt_version
import BuildVersions.kotlin_version
import BuildVersions.lifecycle_version
import BuildVersions.nav_version
import BuildVersions.okhttp_version
import BuildVersions.rxandroid_version
import BuildVersions.rxbinding2_version
import BuildVersions.rxjava_version
import androids.corektxversion

//全局常量
object BuildVersions {
    const val gradle_version = "7.1.2"
    const val kotlin_version = "1.8.0"

    const val nav_version = "2.4.1"
    const val okhttp_version = "4.9.0"
    const val gson_version = "2.8.7"
    const val hilt_version = "2.3.1"
    const val datastore_version = "1.0.0"
    const val lifecycle_version = "2.3.1"
    const val coroutines = "1.6.0"

    const val rxandroid_version="2.0.1"
    const val rxjava_version="2.1.0"
    //操作功能防抖
    const val rxbinding2_version="2.1.1"
}

//应用配置
object androids {
    const val compileSdkV = 32
    const val buildToolsV = "30.0.3"
    const val applicationId = "com.mic"
    const val minSdkV = 21
    const val targetSdkV = 29
    const val vCode = 1
    const val vName = "1.0"
    const val corektxversion="1.6.0"
}

//依赖配置
object Depends {
    //kotlins
    const val kotlinstdlib ="org.jetbrains.kotlin:kotlin-stdlib:${kotlin_version}"
    const val corektxo= "androidx.core:core-ktx:${corektxversion}"
    const val  kotlinxcoroutinesandroid= "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines"
    //view
    const val  appcompat= "androidx.appcompat:appcompat:1.3.0"
    const val  constraint="androidx.constraintlayout:constraintlayout:2.1.3"
    const val  vectordrawable="androidx.vectordrawable:vectordrawable:1.0.1"
    const val  recyclerview= "androidx.recyclerview:recyclerview:1.0.0"
    const val  cardview= "androidx.cardview:cardview:1.0.0"
    const val  material= "com.google.android.material:material:1.5.0"

    //test
    const val  testrunner= "androidx.test.runner.AndroidJUnitRunner"
    const val  junit= "junit:junit:4.12"
    const val  junitandroidx= "androidx.test.ext:junit:1.1.2"
    const val  espresso= "androidx.test.espresso:espresso-core:3.3.0"

    //navigation
    const val  navigationfragment= "androidx.navigation:navigation-fragment:$nav_version"
    const val  navigation= "androidx.navigation:navigation-ui:$nav_version"

    //okhttp
    const val  okhttp= "com.squareup.okhttp3:okhttp:$okhttp_version"
    const val  logginginterceptor= "com.squareup.okhttp3:logging-interceptor:+"
    const val  gson= "com.google.code.gson:gson:$gson_version"

    //rxs
    const val  rxandroid= "io.reactivex.rxjava2:rxjava:$rxandroid_version"
    const val  rxjava= "io.reactivex.rxjava2:rxandroid:$rxjava_version"
    const val  rxbinding2= "com.jakewharton.rxbinding2:rxbinding:2.1.1"

    //retrofit
    const val  retrofit= "com.squareup.retrofit2:retrofit:2.8.1"
    const val  convertergson= "com.squareup.retrofit2:converter-gson:2.8.1"
    const val  adapterrxjava= "com.squareup.retrofit2:adapter-rxjava2:2.5.0"

    //room数据库
    const val  roomruntime= "android.arch.persistence.room:runtime:1.1.1"
    const val  lifecycleextensions= "android.arch.lifecycle:extensions:1.1.1"
    const val  roomcompiler= "android.arch.persistence.room:compiler:1.1.1"
    const val  lifecyclecompiler= "android.arch.lifecycle:compiler:1.1.1"

    //图片加载
    const val glide="com.github.bumptech.glide:glide:4.14.2"

    //datastore
    const val datastore_core="androidx.datastore:datastore-core:$datastore_version"
    const val datastore_preferences_core="androidx.datastore:datastore-preferences-core:$datastore_version"

    //hilt
    const val hilt= "com.google.dagger:hilt-android:$hilt_version"
    const val htlt_compiler= "com.google.dagger:hilt-android-compiler:$hilt_version"

    //lifecycle
    const val viewmodel="androidx.lifecycle:lifecycle-viewmodel:$lifecycle_version"
    const val livedata="androidx.lifecycle:lifecycle-livedata:$lifecycle_version"
    const val lifecycle_runtime="androidx.lifecycle:lifecycle-runtime:$lifecycle_version"
    const val lifecycle_viewmodel_savedstate="androidx.lifecycle:lifecycle-viewmodel-savedstate:$lifecycle_version"
    const val lifecycle_common_java8="androidx.lifecycle:lifecycle-common-java8:$lifecycle_version"
    const val lifecycle_service ="androidx.lifecycle:lifecycle-service:$lifecycle_version"
    const val lifecycle_process="androidx.lifecycle:lifecycle-process:$lifecycle_version"
}

