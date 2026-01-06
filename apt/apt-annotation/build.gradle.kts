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

    // 注解处理 API
    implementation(libs.javax.annotation.api)

    // AutoService
    compileOnly(libs.auto.service)
    annotationProcessor(libs.auto.service)

    // 或者使用 kapt（如果是 Kotlin 项目）
    // kapt("com.google.auto.service:auto-service:1.0.1")
}