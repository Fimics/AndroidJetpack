plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)   // 如果需要 Kotlin
}

val javaVersion = libs.versions.jvm.version.get().toInt()

java {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}

kotlin {
    jvmToolchain(javaVersion)
}

dependencies {
    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.reflect)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.retrofit2.retrofit)
    implementation(libs.junit)
}