# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

AndroidJetpack 是一个多模块 Android 学习/参考项目，涵盖 Jetpack 组件、BLE 蓝牙配网、依赖注入、协程、RxJava、注解处理器、NDK/Python 集成等多个技术方向。主要语言为 Kotlin，项目中大量使用中文注释。根包名为 `com.mic`。

## 构建命令

```bash
# 构建主 app 模块
./gradlew :app:assembleDebug

# 构建指定模块（路径用冒号分隔）
./gradlew :<模块路径>:assembleDebug
# 示例：
./gradlew :ble:ble-client:assembleDebug
./gradlew :di:hilt:assembleDebug
./gradlew :kotlin:kotlin-coroutine:assembleDebug

# 运行单元测试
./gradlew :<模块>:testDebugUnitTest

# 运行单个测试类
./gradlew :<模块>:testDebugUnitTest --tests "com.mic.MyTest"

# 运行 Android 设备端测试
./gradlew :<模块>:connectedDebugAndroidTest

# 清理构建产物
./gradlew clean
```

## 编译环境要求

| 配置项 | 版本 |
|--------|------|
| Compile SDK | 35 (API 35) |
| Min SDK | 24 |
| Target SDK | 35 |
| Kotlin | 2.1.0 |
| AGP (Android Gradle Plugin) | 8.9.0 |
| Java / Kotlin JVM Target | 21 |
| Gradle | 8.9.0 |

所有版本统一在 `gradle/libs.versions.toml` 中管理，构建脚本中通过 `libs.versions.*` 和 `libs.*` 引用。

## 项目架构

### 模块依赖关系

```
app ──────────────┐
app-compose       │
app-navigation ───┤──> libcore（核心共享库）
app-c++           │        │
app-py ───────────┤        ├── OkHttp / Retrofit / Gson（网络）
di/hilt ──────────┤        ├── RxJava3 / Coroutines（异步）
di/dagger ────────┤        ├── Lifecycle / Navigation / DataStore（Jetpack）
netconfig ────────┘        ├── Room（数据库）
                           ├── Glide（图片加载）
log-runtime ──> libcore    ├── XLog（日志）
                           └── NanoHTTPD（内嵌 HTTP 服务器）
```

### 模块详细说明

#### 核心模块

- **app** — 主应用模块，同时使用 Compose + 传统 View 体系（DataBinding + ViewBinding）。包含 Jetpack 各组件的示例代码：
  - `jetpack/lifecycle/` — Lifecycle 生命周期观察
  - `jetpack/livedata/` — LiveData 数据观察
  - `jetpack/viewmodel/` — ViewModel 状态管理
  - `jetpack/databinding/` — DataBinding 数据绑定
  - `jetpack/datastore/` — DataStore 数据持久化
  - `jetpack/room/` — Room 数据库（Entity/Dao/Database）
  - `jetpack/paging/` — Paging 分页加载（DataSource + Adapter + ViewModel）
  - `jetpack/workmanager/` — WorkManager 后台任务

- **libcore** — 核心共享库，通过 `api()` 传递公共依赖给所有消费模块。包含：
  - `utils/KLog.kt` — 统一日志工具
  - `utils/PermissionUtils.kt` — 权限管理
  - `utils/FileTools.kt` / `FileServer.kt` — 文件工具
  - `utils/ExecutorsPoller.kt` / `ThreadUtils.java` — 线程池管理
  - `retroift/RetrofitApi.java` — Retrofit 封装
  - `room/RoomApi.kt` — Room 封装
  - `ex/ActivityEx.kt` / `ExtScope.kt` — Kotlin 扩展函数
  - `jetpack/LiveDataBus.java` / `LiveDataBusX.java` — 事件总线
  - `server/` — 内嵌 NanoHTTPD HTTP 服务器实现（含 SSL、Cookie、请求/响应处理）

- **app-compose** — 纯 Compose UI 示例模块（namespace: `com.mic.compose`）

- **app-navigation** — Jetpack Navigation 导航示例，同时集成了 `com.mic.autolog` 自定义插桩插件和 Python 模块

#### BLE 蓝牙配网模块

- **ble/ble-client** — 手机端（Central 角色），负责扫描 BLE 设备并发送 WiFi 凭证
  - `BleClientActivity.kt` — 主界面，设备列表 + 配网控制
  - `BleClientViewModel.kt` — MVVM 状态管理（StateFlow）
  - `manager/ProvisioningBleManager.kt` — 继承 Nordic BleManager，处理 GATT 操作
  - `scanner/BleScanner.kt` — BLE 设备扫描（Flow）
  - `DeviceAdapter.kt` — RecyclerView 适配器
  - `protocol/GattUUID.kt` — GATT 服务/特征 UUID 定义
  - `model/WiFiCredentials.kt` / `ProvisioningStatus.kt` — 数据模型

- **ble/ble-server** — IoT 设备端（Peripheral 角色），接收凭证并连接 WiFi
  - `BleServerActivity.kt` — 状态显示界面
  - `BleServerViewModel.kt` — MVVM 状态管理
  - `gatt/ProvisioningGattServer.kt` — GATT 服务端实现
  - `advertiser/BleAdvertiser.kt` — BLE 广播管理
  - `wifi/WiFiConnector.kt` — WiFi 连接逻辑（Android 10+ NetworkRequest / 旧版 WifiConfiguration）

- **GATT 协议设计**：
  ```
  Service UUID: 12345678-1234-1234-1234-1234567890AB
  ├── SSID Characteristic (90AC): WRITE — 客户端写入网络名称
  ├── Password Characteristic (90AD): WRITE — 客户端写入密码
  └── Status Characteristic (90AE): READ + NOTIFY — 服务端推送状态
  ```
  状态值：`IDLE` → `RECEIVING_CREDENTIALS` → `CONNECTING_TO_WIFI` → `SUCCESS` / `FAILED`

#### 依赖注入模块

- **di/dagger** — Dagger 2 依赖注入示例
- **di/hilt** — Hilt 依赖注入示例。使用 `dagger.hilt.android` 插件 + `kapt` 注解处理。依赖 `libcore`

#### 注解处理器模块（APT）

- **apt/apt-annotation** — 自定义注解定义
- **apt/apt-compiler** — 注解处理器实现（`CustomProcessor.java`），使用 JavaPoet + AutoService
- **apt/apt-main** — APT 使用示例

#### 网络配网模块

- **netconfig** — IoT 设备网络配置模块（namespace: `com.mic.netconfig`），包含多种配网方式：
  - `softap/` — SoftAP 热点配网（`SoftAPManager.java`、`WiFiConfigService.java`、`UDPServer.java`）
  - `airkiss/` / `smartconfig/` / `ble/` — 预留的其他配网方式目录
  - 使用 Dagger 2 进行依赖注入（非 Hilt）

#### Kotlin / 协程模块

- **kotlin/kotlin-java** — Kotlin 语言特性 + MVVM 模式示例
- **kotlin/kotlin-coroutine** — 纯 Kotlin/JVM 模块（`java-library` 插件），协程使用示例

#### RxJava 模块

- **rx/rx2** — RxJava 2 操作符示例
- **rx/rx3** — RxJava 3 操作符示例

#### 字节码插桩（build-logic）

- **build-logic** — 独立的 Gradle 构建（`includeBuild("build-logic")`），不是普通子模块。包含自定义 Gradle 插件：
  - 插件 ID：`com.mic.autolog`
  - 入口类：`AutoLogPlugin.kt` — 使用 Android Instrumentation API 注册 ASM ClassVisitor
  - `AutoLogClassVisitorFactory.kt` — ASM ClassVisitor 工厂
  - `AutoLogClassVisitor.kt` — 字节码访问器，在方法 enter/exit/error 时插入日志
  - `AutoLogParams.kt` — 插件参数配置
  - 只对 Debug 变体插桩，只对 `com.mic` 包下的类生效
  - 排除 `com.mic.log.runtime`（避免递归插桩）和 DataBinding 生成类

- **log-runtime** — AutoLog 插桩的运行时库（`com.mic.log.runtime.AutoLog`），提供插桩代码调用的日志方法。依赖 `libcore`

#### 其他模块

- **app-c++** — NDK C++ 集成示例
- **app-py** — Chaquopy Python 集成示例（Python 版本 3.10，Chaquopy 17.0.0）
- **extras/libc++** — C++ 原生库
- **extras/libpy** — Python 库封装
- **library** — 通用工具库
- **libspeech** — 语音相关库
- **template** — 模块模板
- **native/** — NDK 相关模块（JNI、CMake、FFmpeg、RTMP、OpenGL），当前已注释掉未启用

## 关键开发规范

### Gradle 构建配置

- 所有依赖版本集中在 `gradle/libs.versions.toml`，构建脚本中**不要硬编码版本号**
- 根 `build.gradle.kts` 通过 `subprojects` 统一配置所有子模块的 Java/Kotlin 兼容性（source/target/jvmTarget 均为 21）
- 根 `build.gradle.kts` 中通过 `resolutionStrategy.force()` 统一解决依赖版本冲突（涉及 AndroidX、Kotlin stdlib、RxJava 等 20+ 个库）
- `build-logic` 是 `includeBuild`（独立构建），使用 `kotlin-dsl` + `java-gradle-plugin` 插件

### 依赖引用模式

- **libcore 使用 `api()` 传递依赖**：消费模块只需 `implementation(project(":libcore"))` 即可获得 OkHttp、Retrofit、Gson、RxJava3、Room、Glide、Lifecycle、DataStore、Navigation、XLog 等全部公共依赖
- **Hilt 模块**：需要同时应用 `dagger.hilt.android` 插件并使用 `kapt` 处理注解
- **Dagger 模块**：直接声明 `dagger` + `dagger-compiler` 的 kapt 依赖
- **BLE 模块**：使用 Nordic BLE 库（`no.nordicsemi.android:ble:2.11.0` + `ble-ktx`）

### UI 构建方式

- **app 模块**：Compose + DataBinding + ViewBinding 混合使用
- **app-compose**：纯 Compose
- **其他模块**（hilt、dagger、netconfig、app-navigation）：ViewBinding + DataBinding
- Compose 编译器扩展版本配置在 `libs.versions.toml` 的 `kotlin-compiler-extension`

### 命名空间

每个模块有独立的 namespace，在各自 `build.gradle.kts` 的 `android.namespace` 中定义：
- app → `com.mic`
- app-compose → `com.mic.compose`
- app-navigation → `com.mic.apppy`
- di/hilt → `com.mic.hilt`
- netconfig → `com.mic.netconfig`
- ble/ble-client → `com.mic.ble.client`
- ble/ble-server → `com.mic.ble.server`
- libcore → `com.mic.libcore`
- log-runtime → `com.mic.log.runtime`

### APK 输出命名

多个 application 模块自定义了 APK 输出文件名格式：`Jetpack${versionName}_${buildType}.apk`

## 技术文档索引

项目中包含大量技术文档（中文）：
- `ble/README.md` — BLE 配网模块总览
- `ble/BLE配网技术分析.md` — BLE 协议栈、配网方案对比、安全架构
- `ble/Nordic_BLE库深度分析.md` — Nordic 库内部机制、请求队列、BleManager 生命周期
- `ble/BLE技术生态.md` — BLE 5.x 特性、Mesh、安全机制、已知 Android 问题
- `netconfig/*.md` — SoftAP、SmartConfig/AirKiss、BLE 等配网方案文档
- `kotlin/kotlin-coroutine/kotlin-coroutine.md` — 协程学习笔记
- `rx/rx2/*.md` / `rx/rx3/*.md` — RxJava 操作符手册
- `di/dagger/dagger.md` / `di/hilt/hilt.md` — DI 框架笔记
- `apt/apt.md` — 注解处理器笔记