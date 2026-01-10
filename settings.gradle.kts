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



