// 测试工具库（纯 JVM）：被各模块以 testImplementation(project(":libs:lib-test")) 引入。
plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // 以 api 暴露，使用方拿到 JUnit / 协程测试 / MockWebServer 全套
    api(libs.junit)
    api(libs.kotlinx.coroutines.test)
    api(libs.mockwebserver)
    api(libs.gson)
}
