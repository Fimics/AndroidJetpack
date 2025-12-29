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
//apply from: 'versions.gradle'
rootProject.name = "AndroidJetpack"
include(":app")
include(":libcore")
//include(":libs:imagemaster")
//include(":xdagger")
//include(":xhilt")
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
//include(":app-system")
