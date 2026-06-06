plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.mic.guide.support.ble"
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig { minSdk = libs.versions.minSdk.get().toInt() }
}

dependencies {
    implementation(project(":libs:lib-log"))
    // Nordic 扫描兼容库：抹平各版本 BLE 扫描差异；连接/GATT 用 Android 原生 BluetoothGatt
    api(libs.nordic.scanner)
    implementation(libs.kotlinx.coroutines.android)
}
