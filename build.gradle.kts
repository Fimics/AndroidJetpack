
buildscript {
//    ext {
//        hilt_version = '1.6.10'
//    }

    dependencies {
        classpath ("com.android.tools.build:gradle:${BuildVersions.gradle_version}")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.38")
    }
}

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.1.2" apply false
    id("com.android.library") version "7.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.6.21" apply false
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