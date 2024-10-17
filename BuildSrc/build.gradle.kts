apply {
    plugin("kotlin")
}

buildscript {
    repositories {
        gradlePluginPortal()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.0")
    }
}
dependencies {
    gradleKotlinDsl()
}
repositories {
    gradlePluginPortal()
}