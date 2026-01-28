pluginManagement {
    repositories {
        google()
        mavenCentral()
        jcenter()
        gradlePluginPortal()
        maven { url = uri("https://www.jitpack.io") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        jcenter()
        mavenCentral()
    }
}

rootProject.name = "AndroidJetpack"
include(":template")
include(":app")
include(":app-c++")
include(":app-py")
include(":libcore")
include(":app-compose")
//include(":libs:imagemaster")
include("di:dagger")
include("di:hilt")
include(":netconfig")
include(":apt:apt-main")
include(":apt:apt-compiler")
include(":apt:apt-annotation")
include(":kotlin:kotlin-java")
include(":kotlin:kotlin-coroutine")
include(":rx:rx2")
include(":rx:rx3")
include(":ble")
include(":extras:libc++")
include(":extras:libpy")
include(":log-runtime")

/**
 * >>>>>>把 build-logic 这个目录声明成一个「独立的 Gradle Build」，让它能：
 * 1.自己管理 插件仓库 / 依赖仓库
 * 2.正常编译 自定义 Gradle 插件
 * 3.被主工程通过 includeBuild("build-logic") 引入
 * 4.没有这个文件，build-logic 不是一个合法的 build，Gradle 会直接报错或行为异常
 * 5.“这里是一个新的 build，不是主工程的子模块”
 */
includeBuild("build-logic")

//include("libs:aliyunplayerres")
//include("libs:common")
//include("libs:jiguang")
//include("libs:net")
//include("libs:silicompressor")
//include(":native:n1_jnibase")
//include(":native:n2_cmake")
//include(":native:n3_ffmpeg")
//include(":native:n4_ffmpeg_bad")
//include(":native:n5_rtmp")
//include(":native:n6_opengl")



