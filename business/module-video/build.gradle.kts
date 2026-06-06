import com.android.build.gradle.BaseExtension

// 组件化双模式开关（docs/01-arch.md §15.3）：
// runAlone.video=true 时本模块作为独立 App(application) 运行调试；否则作为 library 被壳工程集成。
val runAlone = (project.findProperty("runAlone.video") as String?)?.toBoolean() ?: false

if (runAlone) {
    apply(plugin = "com.android.application")
} else {
    apply(plugin = "com.android.library")
}
apply(plugin = "org.jetbrains.kotlin.android")
apply(plugin = "org.jetbrains.kotlin.kapt")   // Room 注解处理（本模块自有特征数据库）

configure<BaseExtension> {
    namespace = "com.mic.guide.module.video"
    compileSdkVersion(libs.versions.compileSdk.get().toInt())

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        if (runAlone) {
            applicationId = "com.mic.guide.video.dev"
            targetSdk = libs.versions.targetSdk.get().toInt()
            versionCode = 1
            versionName = "1.0"
        }
    }

    buildFeatures.viewBinding = true

    // 组件模式：把独立运行入口(src/runalone)并入编译；集成(library)模式下不参与，避免向宿主泄漏 LAUNCHER。
    if (runAlone) {
        sourceSets.getByName("main") {
            manifest.srcFile("src/runalone/AndroidManifest.xml")
            java.srcDir("src/runalone/java")
        }
    }
}

dependencies {
    // arch 以 api 暴露：业务页面继承 arch 的 Base 类
    "api"(project(":arch"))

    // 命令式 apply 后无类型化访问器，依赖配置名用字符串
    "implementation"(libs.androidx.fragment.ktx)            // by viewModels()
    "implementation"(libs.androidx.navigation.fragment.ktx) // nav 子图
    "implementation"(libs.androidx.recyclerview)
    "implementation"(libs.androidx.constraintlayout)        // ConstraintLayout 页面布局
    "implementation"(libs.androidx.paging.runtime)          // Paging 3 分页（PagingSource/PagingDataAdapter）
    // 本模块自有特征数据库（Room）+ RemoteMediator：分页离线缓存（§9.2）
    "implementation"(libs.androidx.room.runtime)
    "implementation"(libs.androidx.room.ktx)                // withTransaction
    "implementation"(libs.androidx.room.paging)             // Room 生成 PagingSource<Int, Entity>
    "kapt"(libs.androidx.room.compiler)
    "implementation"(project(":support:support-network"))   // VideoApiService + NetworkClient（真实网络）
    "implementation"(project(":api:api-player"))            // 复用 support-media 的播放能力（经接口，§6.6）
    "implementation"(project(":libs:lib-image"))            // ImageLoader（Glide 门面）加载缩略图
}
