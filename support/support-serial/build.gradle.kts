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
    // ByteUtils（十六进制转换，迁移自 libcore）
    implementation(project(":libs:lib-common"))
    // licheedev 串口库（含 native .so）：SerialPort builder + SerialPortFinder
    api(libs.serialport)
    implementation(libs.kotlinx.coroutines.android)
}
