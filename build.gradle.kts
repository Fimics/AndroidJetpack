plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.jetbrains.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt) apply false
    id("org.jetbrains.kotlin.jvm") version "2.1.0" apply false
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

// 版本冲突处理控制
subprojects {
    configurations.all {
        resolutionStrategy {
            // 1. 强制统一图中显示的所有冲突版本
            force(
                // ====== Activity 系列（你的主要冲突） ======
                "androidx.activity:activity:1.9.3",
                "androidx.annotation:annotation:1.7.1",
                "androidx.appcompat:appcompat:1.6.1",
                "androidx.arch.core:core-runtime:2.2.0",
                "androidx.collection:collection-ktx:1.4.0",
                "androidx.compose.animation:animation:1.6.6",
                "androidx.compose.material3:material3-android:1.2.0",
                "androidx.compose.material:material-icons-core-android:1.6.6",
                "androidx.constraintlayout:constraintlayout:2.0.1",
                "androidx.core:core-ktx:1.13.1",
                "androidx.customview:customview:1.1.0",
                "androidx.drawerlayout:drawerlayout:1.1.1",
                "androidx.fragment:fragment:1.5.1",
                "androidx.recyclerview:recyclerview:1.2.0",
                "androidx.transition:transition:1.4.1",
                "com.google.code.findbugs:jsr305:3.0.2",
                "com.google.errorprone:error_prone_annotations:2.18.0",
                "com.google.j2objc:j2objc-annotations:2.8",
                "io.reactivex.rxjava2:rxjava:2.2.0",
                "io.reactivex.rxjava2:rxandroid:2.1.0",
                "org.checkerframework:checker-qual:3.33.0",
                "org.jetbrains.kotlin:kotlin-stdlib:2.1.0",
                "org.jetbrains.kotlin:kotlin-stdlib-jdk8:2.1.0",
                "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0",
                "org.jetbrains:annotations:23.0.0",
                "org.reactivestreams:reactive-streams:1.0.4"
            )
        }
    }
}