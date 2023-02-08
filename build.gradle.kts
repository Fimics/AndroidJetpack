
buildscript {
//    ext {
//        hilt_version = '1.6.10'
//    }

    dependencies {
        classpath ("com.android.tools.build:gradle:${BuildVersions.gradle_version}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.40.1")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version BuildVersions.gradle_version apply false
    id("com.android.library") version BuildVersions.gradle_version apply false
    id("org.jetbrains.kotlin.android") version BuildVersions.kotlin_version apply false
}

//groovy
//task clean(type: Delete) {
//    delete rootProject.buildDir
//}

//kotlin
tasks{
    val clean by registering(Delete::class){
        delete(buildDir)
    }
}