apply {
    plugin("kotlin")
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    }
}
dependencies {
    gradleKotlinDsl()
    kotlin("stdlib", "1.8.0")
}
repositories {
    gradlePluginPortal()
}