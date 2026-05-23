# Gradle 构建规范

## 版本管理
- 所有依赖版本必须在 `gradle/libs.versions.toml` 中定义
- 构建脚本中通过 `libs.versions.xxx` 和 `libs.xxx` 引用，禁止硬编码版本号
- 添加新依赖流程：先在 `[versions]` 加版本 → 在 `[libraries]` 加坐标 → 在模块 `build.gradle.kts` 中引用

## 依赖声明
- `implementation` — 模块私有依赖（默认选择）
- `api` — 需要传递给消费者的依赖（仅 libcore 使用）
- `kapt` — 注解处理器（Dagger/Hilt/Room compiler）
- `testImplementation` — 单元测试依赖
- `androidTestImplementation` — 设备端测试依赖
- 公共依赖加到 `libcore` 用 `api()` 传递，模块只需 `implementation(project(":libcore"))`

## 模块配置
- compileSdk = 35, minSdk = 24, targetSdk = 35
- Java/Kotlin JVM Target = 21（根 build.gradle.kts 统一配置，模块不需重复）
- 每个模块必须声明独立的 `namespace`
- Application 模块自定义 APK 命名：`Jetpack${versionName}_${buildType}.apk`

## 插件使用
- `com.android.application` — 可运行的 App 模块
- `com.android.library` — 库模块
- `org.jetbrains.kotlin.android` — Kotlin Android 支持
- `org.jetbrains.kotlin.plugin.compose` — Compose 编译器
- `com.google.dagger.hilt.android` + `kotlin-kapt` — Hilt 注入
- `org.jetbrains.kotlin.jvm` — 纯 Kotlin/JVM 模块

## build-logic
- 是 `includeBuild`（独立 Gradle 构建），不是普通子模块
- 使用 `kotlin-dsl` + `java-gradle-plugin`
- 自定义插件 ID：`com.mic.autolog`
- 修改 build-logic 后可能需要单独构建验证

## 禁止项
- 禁止在模块 build.gradle.kts 中硬编码版本号
- 禁止使用已废弃的 `compile` / `testCompile` 配置
- 禁止在 `allprojects {}` 中添加仓库（用 settings.gradle.kts 的 dependencyResolutionManagement）