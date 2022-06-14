apply {
    plugin("kotlin")
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath(kotlin("gradle-plugin", "1.5.21"))
    }
}
dependencies {
    gradleKotlinDsl()
    kotlin("stdlib", "1.5.21")
}
repositories {
    gradlePluginPortal()
}