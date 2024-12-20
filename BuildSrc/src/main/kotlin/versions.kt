import BuildVersions.appcompat_version
import BuildVersions.cardview_version
import BuildVersions.constraintlayout_version
import BuildVersions.kcoroutines_version
import BuildVersions.dagger_version
import BuildVersions.datastore_version
import BuildVersions.glide_version
import BuildVersions.gson_version
import BuildVersions.hilt_version
import BuildVersions.kotlin_version
import BuildVersions.lifecycle_version
import BuildVersions.material_version
import BuildVersions.nav_version
import BuildVersions.okhttp_version
import BuildVersions.paging_version
import BuildVersions.recyclerview_version
import BuildVersions.retrofit_version
import BuildVersions.room_version
import BuildVersions.rxandroid_version
import BuildVersions.rxbinding_version
import BuildVersions.rxjava_version
import BuildVersions.vectordrawable_version
import BuildVersions.work_version
import androids.corektxversion

//全局常量
object BuildVersions {
    const val gradle_version ="7.4.2"
    const val kotlin_version = "1.8.0"

    //kotlin
    const val ktlib_version = "1.9.0"
    const val ktxcore_version = "1.10.1"
    const val kcoroutines_version = "1.6.0"

    //okhttp 相关
    const val okhttp_version = "4.9.0"
    const val gson_version = "2.8.5"
    const val retrofit_version="2.9.0"

    //jetpack
    const val nav_version = "2.5.1"
    const val datastore_version = "1.0.0"
    const val lifecycle_version = "2.5.1"
    const val paging_version="3.0.0"

    //di-> dagger,hilt
    const val hilt_version = "2.45"
    const val dagger_version="2.45"

    //rx
    const val rxandroid_version="2.1.0"
    const val rxjava_version="2.1.0"
    const val rxbinding_version="2.1.0"

    //图片
    const val glide_version="4.14.2"

    //views
    const val appcompat_version="1.4.1"
    const val constraintlayout_version="2.1.3"
    const val vectordrawable_version="1.0.1"
    const val recyclerview_version="1.0.0"
    const val cardview_version="1.0.0"
    const val material_version="1.4.+"

    //room,dataStore
    const val room_version="2.4.0"
    const val data_store_version="1.0.0"

    //workmanager
    const val work_version="2.7.1"

    //camera

    //buletouth
}

//应用配置
object androids {
    const val compileSdkV = 32
    const val buildToolsV = "30.0.3"
    const val applicationId = "com.mic"
    const val minSdkV = 26
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
    const val  kotlinxcoroutinesandroid= "org.jetbrains.kotlinx:kotlinx-coroutines-android:$kcoroutines_version"
    //view
    const val  appcompat= "androidx.appcompat:appcompat:$appcompat_version"
    const val  constraintlayout="androidx.constraintlayout:constraintlayout:$constraintlayout_version"
    const val  vectordrawable="androidx.vectordrawable:vectordrawable:$vectordrawable_version"
    const val  recyclerview= "androidx.recyclerview:recyclerview:$recyclerview_version"
    const val  cardview= "androidx.cardview:cardview:$cardview_version"
    const val  material= "com.google.android.material:material:$material_version"

    //navigation
    const val  navigationfragment= "androidx.navigation:navigation-fragment:$nav_version"
    const val  navigation= "androidx.navigation:navigation-ui:$nav_version"

    //okhttp
    const val  okhttp= "com.squareup.okhttp3:okhttp:$okhttp_version"
    const val  logginginterceptor= "com.squareup.okhttp3:logging-interceptor:$okhttp_version"
    const val  gson= "com.google.code.gson:gson:$gson_version"

    //rxs
    const val  rxandroid= "io.reactivex.rxjava2:rxjava:$rxandroid_version"
    const val  rxjava= "io.reactivex.rxjava2:rxandroid:$rxjava_version"
    const val  rxbinding2= "com.jakewharton.rxbinding2:rxbinding:$rxbinding_version"

    //retrofit
    const val  retrofit= "com.squareup.retrofit2:retrofit:$retrofit_version"
    const val  convertergson= "com.squareup.retrofit2:converter-gson:$retrofit_version"
    const val  adapterrxjava= "com.squareup.retrofit2:adapter-rxjava2:$retrofit_version"


    //图片加载
    const val glide="com.github.bumptech.glide:glide:$glide_version"

    //datastore
    const val datastore="androidx.datastore:datastore-preferences:$datastore_version"
    const val datastore_core="androidx.datastore:datastore-preferences-core:$datastore_version"

    //dagger
    const val dagger="com.google.dagger:dagger:$dagger_version"
    const val dagger_compiler="com.google.dagger:dagger-compiler:$dagger_version"
    const val dagger_android="com.google.dagger:dagger-android:$dagger_version"
    const val dagger_android_support="com.google.dagger:dagger-android-support:$dagger_version"
    const val dagger_android_processor="com.google.dagger:dagger-android-processor:$dagger_version"


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


    //room数据库
    const val  roomruntime= "androidx.room:room-runtime:$room_version"
    const val  roomcompiler= "androidx.room:room-compiler:$room_version"
    // optional - Kotlin Extensions and Coroutines support for Room
    const val  roomktx= "androidx.room:room-ktx:$room_version"
    const val  roomtesting= "androidx.room:room-testing:$room_version"

    //paging
    const val paging ="androidx.paging:paging-runtime:$paging_version"

    //work
    const val work_java="androidx.work:work-runtime:$work_version"
    const val work_ktx="androidx.work:work-runtime-ktx:$work_version"


    //test
//    const val  testrunner= "androidx.test.runner.AndroidJUnitRunner"
//    const val  junit= "junit:junit:4.12"
//    const val  junitandroidx= "androidx.test.ext:junit:1.1.2"
//    const val  espresso= "androidx.test.espresso:espresso-core:3.3.0"
}

