plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.serial"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    implementation(project(":libs:lib-log"))
    // licheedev 串口库（含 native .so）：SerialPort builder + SerialPortFinder
    api(libs.serialport)
    implementation(libs.kotlinx.coroutines.android)
}
