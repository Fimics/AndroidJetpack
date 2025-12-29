plugins {
    id("com.android.application") version "8.9.0" apply false
    id("com.android.library") version "8.9.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.0" apply false
//    id("com.google.dagger.hilt.android") version BuildVersions.hilt_version apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.57.2")
    }
}

subprojects {
    // 配置 Android Application 模块
    plugins.withId("com.android.application") {
        configure<com.android.build.gradle.AppExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        // 配置 Kotlin 编译选项
        plugins.withId("org.jetbrains.kotlin.android") {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = "21"
            }
        }
    }

    // 配置 Android Library 模块
    plugins.withId("com.android.library") {
        configure<com.android.build.gradle.LibraryExtension> {
            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_21
                targetCompatibility = JavaVersion.VERSION_21
            }
        }
        // 配置 Kotlin 编译选项
        plugins.withId("org.jetbrains.kotlin.android") {
            tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
                kotlinOptions.jvmTarget = "21"
            }
        }
    }

    // 配置纯 Kotlin/JVM 模块
    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            kotlinOptions.jvmTarget = "21"
        }
        tasks.withType<JavaCompile> {
            sourceCompatibility = "21"
            targetCompatibility = "21"
        }
    }
}
