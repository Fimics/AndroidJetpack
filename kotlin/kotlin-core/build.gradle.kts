plugins {
    id("java-library")
    alias(libs.plugins.kotlin.jvm)   // 如果需要 Kotlin
    id("kotlin-kapt")  // 如果需要 Kotlin 注解处理
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

}