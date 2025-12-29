plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.android.gradle.plugin)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.hilt.gradle.plugin)
    }
}

subprojects {
    // 配置 Android Application 模块
    plugins.withId("com.android.application") {
        configure<com.android.build.gradle.AppExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.source.compatibility.get()}")
                targetCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.target.compatibility.get()}")
            }
        }
        // 配置 Kotlin 编译选项
        plugins.withId("org.jetbrains.kotlin.android") {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = libs.versions.kotlin.jvm.target.get()
            }
        }
    }

    // 配置 Android Library 模块
    plugins.withId("com.android.library") {
        configure<com.android.build.gradle.LibraryExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.source.compatibility.get()}")
                targetCompatibility = JavaVersion.valueOf("VERSION_${libs.versions.java.target.compatibility.get()}")
            }
        }
        // 配置 Kotlin 编译选项
        plugins.withId("org.jetbrains.kotlin.android") {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = libs.versions.kotlin.jvm.target.get()
            }
        }
    }

    // 配置纯 Kotlin/JVM 模块
    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.jvmTarget = libs.versions.kotlin.jvm.target.get()
        }
        tasks.withType<JavaCompile> {
            sourceCompatibility = libs.versions.java.source.compatibility.get()
            targetCompatibility = libs.versions.java.target.compatibility.get()
        }
    }
}