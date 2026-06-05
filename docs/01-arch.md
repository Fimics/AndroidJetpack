# Android 多模块项目结构与架构设计

> 工程名：**AiGuide** ｜ 根包名：**`com.mic.guide`** ｜ 构建脚本：**Kotlin DSL（`.gradle.kts`）** ｜ 版本管理：**Version Catalog（`gradle/libs.versions.toml`）**

本文档既是 **架构蓝图**（目标形态），也是 **现状说明**（已落地多少）。请按「状态图例」区分「已实现」与「待落地」，不要把蓝图误当现状。

### 落地进度（状态图例）

| 图例 | 含义 |
| --- | --- |
| ✅ | 已实现并可用 |
| 🟡 | 部分实现 |
| ⬜ | 仅在 `settings.gradle.kts` 声明，**磁盘无目录、无 `build.gradle.kts`** |

| 层级 | 模块 | 状态 | 说明 |
| --- | --- | --- | --- |
| 壳工程 | `app` | ✅ | `MainActivity` / `GuideApp` / BottomNavigation 菜单 / 基础资源已就绪 |
| 架构层 | `arch` | 🟡 | Base 类 + MVC/MVP/MVVM/MVI 四套模板已实现（Activity+Fragment 已对称）；全面 **协程 + Flow**（`StateFlow`/`SharedFlow`/`collectIn`）；**无 `di/`、`config/`、`BaseDialog`**，也**未引入 Hilt** |
| 接口层 | `api/*` | ⬜ | `api-player` / `api-chat` / `api-music` / `api-settings` 均未建目录 |
| 业务层 | `business/*` | 🟡 | `module-home/chat/music/video/settings` 五个**组件化骨架已建**（双模式 + `ComponentApplication`/SPI 自注册，见 §15，已过 `:app:assembleDebug`）；业务 UI/data 待填 |
| 支撑层 | `support/*` | 🟡 | `support-router` 已建（`AppNavigator` 门面 + `Routes` + `NavigatorProvider`，deepLink 跳转 + 失败降级，已真机验证）；`network/websocket/storage/database/permission` 待建 |
| 工具层 | `libs/*` | ⬜ | `lib-common/ui/log/image/extension` 均未建目录 |

> ⚠️ **构建现状**：`settings.gradle.kts` `include` 了 22 个子模块，磁盘上已建 `:app` `:arch` 与 5 个 `:business:module-*`（共 **7 个**），`:app:assembleDebug` **已通过**。其余 **15 个 `api`/`support`/`libs` 模块**仍无目录、无 `build.gradle.kts`——它们被当作「空项目」容忍，因当前无人依赖故不报错；但**在被 `implementation(project(...))` 依赖前必须补骨架或注释掉其 `include`**，否则报 `does not exist`。修复办法见 **§14 落地修复步骤**。

---

## 本文导读

| § | 章节 | 一句话内容 |
| --- | --- | --- |
| 1 | 项目概述 | 六层架构定位与关键设计原则 |
| 2 | 整体目录结构 | 全模块树（含 ➕ 建议补充）+ 技术栈版本 + `build-logic` |
| 3 | 模块职责说明 | 各层职责边界与单向依赖方向 |
| 4 | 网络请求分层 | `support-network` → Repository → ViewModel（协程 + Flow） |
| 5 | 路由与 Navigation | 单 Activity + 多 NavGraph + 跨模块跳转 |
| 6 | api 能力接口层 | 用接口解耦业务模块（非 HTTP） |
| 7 | arch 架构层 | Base 类 + MVC/MVP/MVVM/MVI 真实 API 速查 |
| 8 | business 标准结构 | 业务模块标准目录与规则 |
| 9 | support 支撑模块 | 垂直能力模块清单 |
| 10 | 端到端数据流 | 首页→聊天详情的完整链路 |
| 11 | 依赖规则与通信 | 依赖引入方式 + 三种模块通信 |
| 12 | 开发与构建规范 | 构建 / Flow / DI / 命名约定 |
| 13 | 落地任务清单 | 据实校正的进度清单 |
| 14 | 落地修复步骤 | 让工程可 sync / 编译的最小步骤 |
| 15 | 组件化模块可插拔 | 集成/组件双模式 + ServiceLoader 自注册 + 拔插清单 |

---

## 1. 项目概述

这是一个采用 **多模块化（Modularization）** 架构的 Android 项目，核心目标是 **高内聚、低耦合**，便于团队并行开发、独立编译和长期维护。

项目从职责上分为六层：

| 层级 | 模块 | 定位 |
| --- | --- | --- |
| 壳工程 | `app` | 组装入口、初始化、NavGraph 汇总，**不写业务** |
| 接口层 | `api` | 业务对外**能力接口**，模块间解耦（**非 HTTP**） |
| 架构层 | `arch` | MVC / MVP / MVVM / MVI 基础框架与 Base 类 |
| 业务层 | `business` | 各业务模块，只关心自身业务 |
| 支撑层 | `support` | 网络、存储、数据库、路由等垂直能力 |
| 工具层 | `libs` | 通用工具、UI、日志、图片、扩展 |

### 关键设计原则

1. **业务模块之间禁止直接依赖**，只能通过 `api`（能力接口）+ 路由 / 事件总线通信。
2. **`api` 是能力暴露层，不是 HTTP 网络接口**（HTTP/Retrofit 在 `support-network` + 各业务 `data/remote`）。
3. **所有页面必须继承 `arch` 中对应架构的 Base 类**，统一 ViewBinding 生命周期、Loading、异常处理等。
4. **依赖方向单向向下**：`business → api / arch / support / libs`，反向不允许。
5. 壳工程 `app` 只做模块组装、初始化、**NavGraph 汇总注册**，不写具体业务。
6. **网络请求禁止出现在 UI 层**（Activity / Fragment / Composable），必须经 Repository 下沉。

---

## 2. 整体目录结构

> 状态标记：**✅** 已存在 ｜ **🟡** 部分实现 ｜ **⬜** 已在 `settings.gradle.kts` 声明、待建目录 ｜ **➕** 建议补充（未声明），其后 **★★★/★★/★** 为落地推荐度。

```
AiGuide/ (根目录)
├── build.gradle.kts                ✅ 根脚本：toolchain(JDK21) + 依赖冲突 force
├── settings.gradle.kts             ✅ 声明全部 23 个模块（多数目录待建）
├── gradle/libs.versions.toml       ✅ Version Catalog（版本/依赖/插件）
├── build-logic/                    ➕★★★ Gradle 约定插件：收敛各模块构建脚本（见 §2.3）
│   └── convention/                 #     Android.Library / Hilt / Compose 等约定插件
├── docs/
│   └── 01-arch.md                  ✅ 本文档
│
├── app/                            ✅ 壳工程：MainActivity / GuideApp / NavHost / 路由汇总
│
├── api/                            ⬜ 业务对外能力接口（解耦用，非 HTTP）
│   ├── api-player/
│   ├── api-chat/
│   ├── api-music/
│   ├── api-settings/
│   ├── api-home/                   ➕★   补：与 module-home 对称
│   └── api-video/                  ➕★   补：与 module-video 对称（当前缺）
│
├── arch/                           🟡 架构基础库（核心，已部分落地）
│
├── business/                       🟡 业务模块（5 个组件化骨架已建，见 §15）
│   ├── module-home/
│   ├── module-chat/
│   ├── module-music/
│   ├── module-video/
│   └── module-settings/
│
├── support/                        ⬜ 支撑模块（垂直能力）
│   ├── support-network/            # HTTP 基础设施（Retrofit 工厂、拦截器、统一响应）
│   ├── support-websocket/          # WebSocket 长连接
│   ├── support-router/             # 路由门面（封装 Jetpack Navigation）
│   ├── support-storage/            # 本地存储（DataStore）
│   ├── support-database/           # 数据库（Room）
│   ├── support-permission/         # 运行时权限
│   ├── support-ai/                 ➕★★★ AI 能力：大模型对话/语音/识别 封装（AiGuide 核心）
│   ├── support-media/              ➕★★  音视频播放（Media3 / ExoPlayer，依赖待补声明）
│   ├── support-ble/                ➕★★  低功耗蓝牙（Nordic BLE）
│   ├── support-serial/             ➕★★  串口通信（serialport）
│   ├── support-camera/             ➕★★  相机（CameraX）
│   ├── support-python/             ➕★★  内嵌 Python 脚本（Chaquopy）
│   ├── support-push/               ➕★   推送
│   └── support-update/             ➕★   应用内升级
│
├── libs/                           ⬜ 通用工具库
│   ├── lib-common/
│   ├── lib-ui/
│   ├── lib-log/
│   ├── lib-image/
│   ├── lib-extension/
│   ├── lib-widget/                 ➕★★  复杂自定义控件（lib-ui 管主题，lib-widget 管控件）
│   └── lib-test/                   ➕★★  测试工具：协程/Flow 规则、Fake、MockWebServer（依赖待补声明）
│
├── common/                         ➕★★  公共资源（colors / strings / dimens / theme）
└── baseline-profile/               ➕★   启动/滚动性能基线（Macrobenchmark，依赖待补声明）
```

> `➕` 模块尚未在 `settings.gradle.kts` 声明，按推荐度逐步引入；其依赖缺口与分层约束见 §2.2。

### 2.1 技术栈与版本（来自 `gradle/libs.versions.toml`）

> 所有版本统一在 Version Catalog 管理；根 `build.gradle.kts` 用 `resolutionStrategy.force` 固定一批易冲突的传递依赖。下表为当前声明的关键版本。

| 类别 | 组件 | 版本 |
| --- | --- | --- |
| 构建 | AGP | 8.9.0 |
| 构建 | Gradle 工具链 JDK | 21（`jvmToolchain(21)`，与启动 JVM 解耦） |
| 构建 | compileSdk / minSdk / targetSdk | 35 / 24 / 35 |
| 语言 | Kotlin | 2.1.0 |
| 语言 | Coroutines | 1.8.0 |
| DI | Hilt / androidx-hilt | 2.57.2 / 1.3.0 |
| 架构组件 | Lifecycle | 2.5.1 |
| 架构组件 | **Navigation** | **2.5.1** |
| 架构组件 | Room | 2.4.0 |
| 架构组件 | DataStore | 1.0.0 |
| 架构组件 | Paging | 3.0.0 |
| 架构组件 | WorkManager | 2.7.1 |
| 网络 | Retrofit / OkHttp | 2.9.0 / 4.11.0 |
| 网络 | Gson / kotlinx-serialization | 2.8.6 / 1.6.0 |
| UI | Material | 1.10.0 |
| UI | Compose BOM | 2024.04.01 |
| 图片 | Glide | 4.14.2 |

**已在版本目录声明、按需启用的扩展能力**（不强制使用，但落地时可直接引）：

| 能力 | 组件 | 版本 |
| --- | --- | --- |
| 嵌入 Python | Chaquopy | 17.0.0 |
| 低功耗蓝牙 | Nordic BLE / Scanner | 2.11.0 / 1.6.0 |
| 串口通信 | serialport | 2.1.5 |
| 相机 | CameraX | 1.3.4 |
| 日志 | xlog | 1.11.0 |
| 事件总线 | LiveEventBus | 1.8.0 |

> 说明：`Chaquopy / BLE / 串口 / CameraX` 表明本工程预期会接入 **Python 脚本与硬件（蓝牙/串口/相机）**。这些能力建议各自下沉到独立 `support-*` 模块，不要散落在业务层。

### 2.2 建议补充模块的依赖与约束

§2 树中标 **➕** 的即建议补充项，职责与推荐度（★）已在树内标注，这里只补两点落地约束：

- **版本目录尚未声明、使用前需先补**：Media3 / ExoPlayer（`support-media`）、MockWebServer（`lib-test`）、`androidx.benchmark.macro`（`baseline-profile`）。其余（Chaquopy、Nordic BLE、serialport、CameraX，见 §2.1）已声明，可直接引。
- **分层一致性**：`support-*` 只提供垂直能力、`api-*` 只放接口；硬件/AI 的业务编排放在 `business/*`，不得渗入 `support`。

### 2.3 build-logic 约定插件（强烈推荐）

多模块工程最容易腐化的地方是**每个 `build.gradle.kts` 重复同一套 android/kotlin/hilt 配置**。`build-logic` 用 Gradle [约定插件（Convention Plugins）] 把这些配置收敛成一处，模块里只写一行 `id("aiguide.android.library")`。

```kotlin
// settings.gradle.kts —— 以独立构建方式引入
pluginManagement {
    includeBuild("build-logic")
}

// build-logic/convention/src/main/kotlin/AndroidLibraryConventionPlugin.kt
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) = with(target) {
        with(pluginManager) {
            apply("com.android.library")
            apply("org.jetbrains.kotlin.android")
        }
        extensions.configure<LibraryExtension> {
            compileSdk = 35
            defaultConfig { minSdk = 24 }
            buildFeatures { viewBinding = true }
        }
        // 统一 JVM toolchain、通用依赖（core-ktx / coroutines …）等
    }
}
```

```kotlin
// business/module-home/build.gradle.kts —— 从十几行收敛到几行
plugins {
    id("aiguide.android.library")     // 约定插件：一行搞定 android+kotlin+viewBinding
    id("aiguide.android.hilt")        // 约定插件：一行接入 Hilt
}
dependencies {
    api(project(":arch"))
    implementation(project(":support:support-network"))
}
```

> 收益：新增业务模块从「复制粘贴一大段构建脚本」变成「应用两个约定插件」，版本/SDK/编译参数全工程一处可改。落地时机：模块数量 > 5 个即开始显著获益。

---

## 3. 模块职责说明

| 模块 | 职责 | 可依赖 |
| --- | --- | --- |
| `app` | 模块组装、`Application` 初始化、**主 NavGraph 汇总**、主题样式。**不写业务代码** | 全部 |
| `api` | 各业务模块对外暴露的能力接口（Kotlin interface），供其他模块调用而不依赖业务实现 | `arch`、`libs` |
| `arch` | 架构核心：Base 类 + MVC/MVP/MVVM/MVI 模板 | `libs` + AndroidX/协程（**不依赖 `support`**） |
| `business/*` | 纯业务模块：UI + Repository + 本模块 ApiService | `api`、`arch`、`support`、`libs` |
| `support/*` | 垂直支撑能力（网络基础设施、路由门面、存储、权限） | `libs` |
| `libs/*` | 与业务无关的通用工具、UI、扩展 | 仅彼此基础依赖 |

> 注：`arch` **不**依赖 `support`。`support` 自身依赖 `libs`，若 `arch` 再反向依赖 `support` 会造成分层倒挂。当前真实 `arch/build.gradle.kts` 也未依赖任何 `support` 模块。

**依赖方向（单向向下）：**

```
              app
               │  组装 NavGraph / 初始化（唯一可依赖全部 business）
   ┌───────────┼───────────────┐
business ────► api              （business 之间不可互相依赖）
   │   │        │
   │   └────────┤
   ▼            ▼
  arch ──────► libs ◄────── support
   │                          │
   └──────────────────────────┘
        （arch 与 support 都建立在 libs 之上，互不依赖）
```

---

## 4. 网络请求分层（重点）

### 4.1 一句话原则

| 层级 | 放什么 | 不放什么 |
| --- | --- | --- |
| `support-network` | Retrofit/OkHttp **工厂**、拦截器、统一响应体、网络异常、Base URL | 具体业务 API 路径 |
| `business/.../data/remote` | 本模块 **Retrofit ApiService**、请求/响应 DTO | UI 逻辑、ViewModel |
| `business/.../data/repository` | **Repository**：组合远程 + 本地，调用 `BaseRepository.safeCall` | 直接操作 View |
| `business/.../ui`（ViewModel） | 调用 Repository，处理 `Result` / 状态 | Retrofit、OkHttp、JSON 解析 |

> `api-xxx` 模块**绝不**放 HTTP 接口；它只放「播放音乐」「跳转聊天」这类**业务能力**接口。

### 4.2 调用链路

```
┌─────────────┐     ┌───────────────────┐     ┌─────────────────┐     ┌──────────────────┐
│  Fragment   │────►│    ViewModel      │────►│   Repository    │────►│  ApiService      │
│  / Activity │     │ launchWithLoading │     │  safeCall { }   │     │  (Retrofit)      │
└─────────────┘     └───────────────────┘     └────────┬────────┘     └────────┬─────────┘
                                                       │                        │
                                                       ▼                        ▼
                                              ┌──────────────────┐    ┌──────────────────┐
                                              │ Room / DataStore │    │ support-network  │
                                              │   (本地缓存)      │    │ OkHttp + 拦截器  │
                                              └──────────────────┘    └──────────────────┘
```

### 4.3 support-network 模块结构（规划 ⬜）

```
support/support-network/
└── src/main/java/com/mic/guide/support/network/
    ├── client/
    │   ├── NetworkClient.kt          # Retrofit 单例工厂
    │   └── OkHttpClientFactory.kt    # 超时、连接池、拦截器装配
    ├── interceptor/
    │   ├── AuthInterceptor.kt        # Token 注入
    │   ├── LoggingInterceptor.kt     # 日志（Debug 可开 Body）
    │   └── ErrorInterceptor.kt       # HTTP 状态码统一处理
    ├── model/
    │   ├── ApiResponse.kt            # 统一后端包装：code / message / data
    │   └── NetworkException.kt       # 网络层异常
    └── config/
        └── NetworkConfig.kt          # BaseUrl、超时、是否 Debug
```

**职责边界**：`support-network` 只提供「怎么发请求」，不提供「请求哪个业务接口」。

```kotlin
// support-network/.../ApiResponse.kt
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?,
) {
    fun isSuccess(): Boolean = code == 0
}

// support-network/.../NetworkClient.kt
object NetworkClient {
    fun <T> createService(serviceClass: Class<T>, baseUrl: String): T {
        // OkHttp + Retrofit + Gson / kotlinx-serialization
        TODO()
    }
}
```

### 4.4 业务模块 data 层结构（以 module-home 为例，规划 ⬜）

```
business/module-home/
└── src/main/java/com/mic/guide/module/home/
    ├── data/
    │   ├── remote/
    │   │   ├── HomeApiService.kt       # Retrofit 接口（本模块 HTTP 端点）
    │   │   └── dto/
    │   │       ├── BannerDto.kt
    │   │       └── FeedDto.kt
    │   ├── local/
    │   │   ├── HomeDao.kt              # Room（可选）
    │   │   └── HomePreferences.kt      # DataStore（可选）
    │   ├── mapper/
    │   │   └── HomeMapper.kt           # Dto → Domain Model
    │   └── repository/
    │       └── HomeRepository.kt       # 继承 BaseRepository
    ├── domain/                         # 可选：纯 Kotlin 业务模型 / UseCase
    │   └── model/
    │       └── FeedItem.kt
    ├── ui/
    │   ├── HomeFragment.kt
    │   └── HomeViewModel.kt
    └── di/
        └── HomeModule.kt               # Hilt：提供 ApiService / Repository
```

### 4.5 完整代码示例（与真实 arch API 对齐）

> 重点（全面 Flow）：业务 VM 继承 `MvvmViewModel`（即 `BaseViewModel`），用 `launchWithLoading { }`；UI 状态用 **`StateFlow`** 暴露；基类的 `loading` 是 `StateFlow<Boolean>`、`error` 是 `SharedFlow<Throwable>`（一次性事件），由基类自动 `collectIn` 收集。MVI VM 继承 `MviViewModel<I,S,E>(initial)`，外部 `dispatch(intent)`，内部 `setState { }` / `sendEffect(...)`。

```kotlin
// ── 1. 业务 Retrofit 接口（business/module-home/data/remote）──
interface HomeApiService {
    @GET("home/feed")
    suspend fun getFeed(@Query("page") page: Int): ApiResponse<List<FeedDto>>
}

// ── 2. Repository（business/module-home/data/repository）──
// 注意 BaseRepository.safeCall 返回 Kotlin 标准库 Result<T>，并已切到 IO 线程
class HomeRepository @Inject constructor(
    private val api: HomeApiService,
    private val dao: HomeDao?,          // 可选本地缓存
) : BaseRepository() {

    suspend fun loadFeed(page: Int): Result<List<FeedItem>> = safeCall {
        val response = api.getFeed(page)
        if (!response.isSuccess()) {
            throw NetworkException(response.code, response.message)
        }
        val items = response.data.orEmpty().map { it.toDomain() }
        dao?.insertAll(items)          // 写缓存（可选）
        items
    }

    suspend fun loadFeedFromCache(): Result<List<FeedItem>> = safeCall {
        dao?.getAll()?.map { it.toDomain() }.orEmpty()
    }
}

// ── 3. MVVM ViewModel（business/module-home/ui）—— 用 StateFlow 暴露 UI 状态 ──
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: HomeRepository,
) : MvvmViewModel() {                          // = BaseViewModel，自带 loading/error/launchWithLoading

    private val _feed = MutableStateFlow<List<FeedItem>>(emptyList())
    val feed: StateFlow<List<FeedItem>> = _feed.asStateFlow()

    fun refresh(page: Int = 1) {
        launchWithLoading {                    // 自动管理 loading，异常自动进 error(SharedFlow)
            repository.loadFeed(page)
                .onSuccess { _feed.value = it }
                // 失败已由 BaseViewModel.error 统一分发，无需在此 try/catch
        }
    }
}

// ── 3b. Fragment 侧：用 collectIn 在 viewLifecycleOwner/STARTED 收集 ──
class HomeFragment : MvvmFragment<FragmentHomeBinding, HomeViewModel>() {
    override val viewModel: HomeViewModel by viewModels()
    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun observe() {
        viewModel.feed.collectIn(viewLifecycleOwner) { binding.list.submit(it) }
    }
    // loading / error 已由 MvvmFragment 基类自动 collectIn，子类可覆写 onLoading/onError
}

// ── 4. MVI 写法：网络结果进 State，导航进 Effect ──
@HiltViewModel
class HomeMviViewModel @Inject constructor(
    private val repository: HomeRepository,
) : MviViewModel<HomeIntent, HomeState, HomeEffect>(HomeState()) {

    override fun handleIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadFeed -> launchWithLoading {
                repository.loadFeed(intent.page)
                    .onSuccess { setState { copy(feed = it) } }   // 不可变 State
            }
            is HomeIntent.OpenDetail -> sendEffect(HomeEffect.NavigateToDetail(intent.id))
        }
    }
}
```

> **DI 提示**：以上 `@HiltViewModel` / `@Inject` 依赖 Hilt。**`arch` 目前未引入 Hilt**，因此每个 `business`/`support` 模块需在自己的 `build.gradle.kts` 自带 Hilt 插件与依赖（见 §8）。若暂不引 Hilt，可在 `di/` 或 `app` 里手动 `new` 装配。

### 4.6 网络相关禁止事项

| ❌ 禁止 | ✅ 正确做法 |
| --- | --- |
| Fragment 里直接 `Retrofit.create()` | 通过 Hilt 注入 Repository |
| ViewModel 里写 OkHttp/Retrofit 调用 | ViewModel 只调 Repository |
| 在 `api-xxx` 定义 `@GET` 接口 | HTTP 接口放各业务 `data/remote` |
| 多个业务模块共用一个巨大 `ApiService` | 按业务模块拆分 ApiService |
| UI 层解析 JSON / 处理 HTTP 状态码 | 在 Repository 或 `support-network` 拦截器统一处理 |

### 4.7 依赖配置（规划 ⬜）

```kotlin
// support/support-network/build.gradle.kts
dependencies {
    implementation(libs.retrofit2.retrofit)
    implementation(libs.okhttp)
    implementation(libs.retrofit2.converter.gson)
    implementation(project(":libs:lib-log"))
}

// business/module-home/build.gradle.kts
dependencies {
    api(project(":arch"))
    implementation(project(":support:support-network"))
    implementation(project(":support:support-database"))  // 若用 Room
    // Hilt：因 arch 不传递 Hilt，业务模块需自带
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}
```

---

## 5. 路由与 Jetpack Navigation（重点）

> 当前工程 Navigation 版本为 **2.5.1**（见 §2.1）。`app` 已有 `res/menu/bottom_nav.xml`，为单 Activity + BottomNavigation 结构打下基础。

### 5.1 设计目标

多模块下，**业务模块彼此不依赖**，但需要：
- 模块 A 跳转到模块 B 的页面；
- 统一传参、返回结果、Deep Link；
- 避免 `startActivity` 硬编码类名。

**方案**：`support-router` 封装 Jetpack Navigation，业务模块只暴露 **Route 常量 + Fragment**，`app` 负责 **汇总 NavGraph**。

### 5.2 职责划分

| 模块 | 导航相关职责 |
| --- | --- |
| `support-router` | `Routes` 常量、`AppNavigator` 门面、`navigateSafe` 降级扩展、跨模块 deepLink 跳转 API |
| `business/module-xxx` | 提供 Fragment 实现；可选提供 `xxx_nav_graph.xml` |
| `app` | `MainActivity` + 主 `NavHostFragment`；**include 各业务子图**；注册 Deep Link |

### 5.3 推荐导航架构：单 Activity + 多 NavGraph

```
app/MainActivity
└── NavHostFragment  (nav_graph_main.xml)
    ├── include home_nav_graph      ← module-home 提供
    ├── include chat_nav_graph      ← module-chat 提供
    ├── include music_nav_graph
    └── include settings_nav_graph
```

### 5.4 support-router 模块结构（🟡 已落地）

> 已建并真机验证：`AppNavigator` 经 **deepLink + 安全降级** 跨模块跳转，业务层只依赖 `support-router`、不依赖目标业务模块（印证 §15.1 支柱②）。下面是**磁盘上的真实代码**，照抄即可。

```
support/support-router/
└── src/main/java/com/mic/guide/support/router/
    ├── Routes.kt                   # 全局路由 URI 常量 / 构造器（单一事实源）
    ├── AppNavigator.kt             # 统一跳转门面（持有 NavController + 降级 + 防抖）
    ├── NavigatorProvider.kt        # 全局持有当前 AppNavigator（app 注册 / 置空）
    └── ext/
        └── NavControllerExt.kt     # navigateSafe：deepLink 无目标时降级而非崩溃
```

```kotlin
// support-router/.../Routes.kt —— URI 必须与各业务 nav_graph 的 <deepLink> 一致
object Routes {
    const val SCHEME = "aiguide"
    fun chatDetail(conversationId: String) = "$SCHEME://chat/detail/$conversationId"
}

// support-router/.../ext/NavControllerExt.kt —— 可插拔兜底：目标模块被拔掉时不崩
inline fun NavController.navigateSafe(
    uri: Uri,
    onFail: (Uri, Throwable) -> Unit = { _, _ -> },
) {
    try {
        navigate(uri)                       // 无匹配 deepLink 时抛 IllegalArgumentException
    } catch (e: IllegalArgumentException) {
        onFail(uri, e)
    }
}

// support-router/.../AppNavigator.kt —— 业务层只依赖本类，不依赖目标业务模块
class AppNavigator(
    private val navController: NavController,
    private val onUnavailable: (Uri) -> Unit = {},   // 目标不可用时降级（Toast / 打点 / 跳默认页）
) {
    private var lastNavMillis = 0L

    fun toChatDetail(conversationId: String) = navigate(Routes.chatDetail(conversationId))

    fun navigate(uriString: String) {
        val now = System.currentTimeMillis()
        if (now - lastNavMillis < 500) return        // 防重复点击
        lastNavMillis = now
        navController.navigateSafe(Uri.parse(uriString)) { uri, _ -> onUnavailable(uri) }
    }

    fun back(): Boolean = navController.navigateUp()
}

// support-router/.../NavigatorProvider.kt —— app 在 Activity onCreate 注册、onDestroy 置空
object NavigatorProvider {
    @JvmStatic
    var navigator: AppNavigator? = null
}
```

> **依赖**：`support-router/build.gradle.kts` 只需 `api(libs.androidx.navigation.runtime.ktx)`（拿 `NavController`）。`app` 在 `MainActivity` 注册：`NavigatorProvider.navigator = AppNavigator(navHost.navController) { uri -> toast("目标暂不可用：$uri") }`，业务侧调用见 §5.7。
>
> **降级即可插拔**：拔掉 `module-chat`（删依赖或注释其子图 `<include>`）后，`toChatDetail` 的 deepLink 解析不到目标 → `navigateSafe` 捕获并走 `onUnavailable`（Toast），App 不崩——已真机验证（见 §15）。

### 5.5 app 模块：主 NavGraph 汇总

```xml
<!-- app/src/main/res/navigation/nav_graph_main.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/nav_graph_main"
    app:startDestination="@id/homeFragment">

  <!-- 方式一：直接声明 Fragment（简单项目） -->
  <fragment
      android:id="@+id/homeFragment"
      android:name="com.mic.guide.module.home.ui.HomeFragment"
      android:label="Home">
      <action
          android:id="@+id/action_home_to_chat_detail"
          app:destination="@id/chatDetailFragment" />
  </fragment>

  <fragment
      android:id="@+id/chatDetailFragment"
      android:name="com.mic.guide.module.chat.ui.ChatDetailFragment"
      android:label="Chat">
      <argument
          android:name="conversationId"
          app:argType="string" />
  </fragment>

  <!-- 方式二：include 子模块导航图（模块变大后推荐） -->
  <include app:graph="@navigation/home_nav_graph" />
  <include app:graph="@navigation/chat_nav_graph" />
</navigation>
```

```kotlin
// app/.../MainActivity.kt —— 磁盘真实代码
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 注册全局路由门面：业务层经 NavigatorProvider.navigator 跳转
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        NavigatorProvider.navigator = AppNavigator(navHost.navController) { uri ->
            // 目标模块被拔掉 / 未集成时降级（§15 可插拔兜底）
            Toast.makeText(this, "目标暂不可用：$uri", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NavigatorProvider.navigator = null // 防止静态持有 NavController/Activity 泄漏
    }
}
```

```xml
<!-- app/src/main/res/layout/activity_main.xml -->
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/nav_host"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:name="androidx.navigation.fragment.NavHostFragment"
    app:navGraph="@navigation/nav_graph_main"
    app:defaultNavHost="true" />
```

### 5.6 业务模块内导航（与真实页面基类对齐）

每个业务模块维护自己的子图和页面。**注意**：页面继承 `arch` 的 `MvvmFragment`，逻辑写在 `initView()` / `observe()` 钩子里（不要直接覆写 `onViewCreated`，否则会绕过基类的 ViewBinding / loading-error 绑定）。

```xml
<!-- business/module-chat/src/main/res/navigation/chat_nav_graph.xml -->
<navigation xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/chat_nav_graph"
    app:startDestination="@id/chatListFragment">

  <fragment
      android:id="@+id/chatListFragment"
      android:name="com.mic.guide.module.chat.ui.ChatListFragment"
      android:label="Messages">
      <action
          android:id="@+id/action_list_to_detail"
          app:destination="@id/chatDetailFragment" />
  </fragment>

  <fragment
      android:id="@+id/chatDetailFragment"
      android:name="com.mic.guide.module.chat.ui.ChatDetailFragment"
      android:label="Chat">
      <argument
          android:name="conversationId"
          app:argType="string" />
  </fragment>
</navigation>
```

```kotlin
// business/module-chat/.../ChatListFragment.kt
class ChatListFragment : MvvmFragment<FragmentChatListBinding, ChatListViewModel>() {

    override val viewModel: ChatListViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentChatListBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rvChat.setOnItemClickListener { conversationId ->
            // 模块内跳转：Safe Args
            val action = ChatListFragmentDirections.actionListToDetail(conversationId)
            findNavController().navigate(action)
        }
    }

    override fun observe() {
        // conversations: StateFlow<List<Conversation>>；loading/error 已由基类自动收集
        viewModel.conversations.collectIn(viewLifecycleOwner) { binding.rvChat.submit(it) }
    }
}
```

### 5.7 跨模块跳转（核心）

业务模块 A 需要打开模块 B 的页面，**不依赖 B 的 Fragment 类**：

> 方式 1 已在 `module-home` 落地并真机验证：`HomeFragment` 的「去聊天(跨模块)」按钮即此调用，`module-home` 零依赖 `module-chat`（详见 §5.4 / §15）。

```kotlin
// 方式 1：通过 support-router 的 AppNavigator（推荐）—— 内部走 deepLink + 降级
NavigatorProvider.navigator?.toChatDetail(conversationId)

// 方式 2：通过 api 能力接口 + 路由实现（与 api 层配合）
// api-chat 定义：
interface ChatApi {
    fun openConversation(conversationId: String)
}
// module-chat 中实现：
class ChatApiImpl(private val navigator: AppNavigator) : ChatApi {
    override fun openConversation(conversationId: String) {
        navigator.toChatDetail(conversationId)
    }
}
// module-home 只依赖 api-chat，不依赖 module-chat：
// chatApi.openConversation(id)
```

### 5.8 MVI 中的导航（副作用）

导航是一次性事件，应走 `MviEffect`，不要写进 `MviState`。在 `MviFragment` 中，副作用统一交给 `handleEffect()`（由基类在 `viewLifecycleOwner` 的 STARTED 周期内收集）：

```kotlin
// 定义（实现 arch 的 MviContract 标记接口）
sealed interface HomeEffect : MviEffect {
    data class NavigateToDetail(val id: String) : HomeEffect
    data class ShowToast(val msg: String) : HomeEffect
}

// HomeFragment 继承 MviFragment，覆写 handleEffect
class HomeFragment :
    MviFragment<FragmentHomeBinding, HomeIntent, HomeState, HomeEffect, HomeMviViewModel>() {

    override val viewModel: HomeMviViewModel by viewModels()

    override fun createBinding(inflater: LayoutInflater, container: ViewGroup?) =
        FragmentHomeBinding.inflate(inflater, container, false)

    override fun renderState(state: HomeState) {
        binding.list.submit(state.feed)
    }

    override fun handleEffect(effect: HomeEffect) {
        when (effect) {
            is HomeEffect.NavigateToDetail ->
                NavigatorProvider.navigator?.toChatDetail(effect.id)
            is HomeEffect.ShowToast ->
                toast(effect.msg)
        }
    }
}
```

### 5.9 Safe Args 与 Gradle 配置

```kotlin
// app/build.gradle.kts 或各 business 模块
plugins {
    alias(libs.plugins.android.application) // 或 android.library
    alias(libs.plugins.navigation.safeargs.kotlin)  // ⚠️ 需先在版本目录与根脚本声明
}

dependencies {
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
}
```

> ⚠️ **待补**：当前 `gradle/libs.versions.toml` 含 `navigation = 2.5.1`，但**尚未声明 Safe Args 的 Gradle 插件**（`androidx.navigation.safeargs.kotlin`）。使用 Safe Args 前需在根 `build.gradle.kts`（或版本目录 `[plugins]`）补声明该插件，否则 `*Directions` 类不会生成。各模块还需在 `res/navigation/` 下放 xml。

### 5.10 BottomNavigation + 多子图（常见主页结构）

`app` 现有 `res/menu/bottom_nav.xml`（菜单项含 home / music / video / me / folder 等图标）。BottomNavigation 的每个 item 对应一个子图，切 tab 时各自保留返回栈：

```xml
<!-- nav_graph_main.xml -->
<navigation ... app:startDestination="@id/home_nav_graph">
  <include app:graph="@navigation/home_nav_graph" android:id="@+id/home_nav_graph" />
  <include app:graph="@navigation/music_nav_graph" android:id="@+id/music_nav_graph" />
  <include app:graph="@navigation/video_nav_graph" android:id="@+id/video_nav_graph" />
  <include app:graph="@navigation/settings_nav_graph" android:id="@+id/settings_nav_graph" />
</navigation>
```

```kotlin
// BottomNavigation 与 NavController 绑定
binding.bottomNav.setupWithNavController(navController)
// 每个 tab 对应一个子图，切换时保留各自返回栈
```

> ⚠️ **命名对齐**：`bottom_nav.xml` 的菜单项 id 需与子图 `startDestination`/规划的 business 模块对应。当前菜单图标命名（home/music/video/me/folder）与规划模块（module-home/chat/music/video/settings）略有出入，落地时请统一（例如 `me`↔`settings`、明确 `folder`/`chat` 归属）。

### 5.11 路由 vs api 能力：何时用哪个

| 场景 | 用路由（Navigation） | 用 api 接口 |
| --- | --- | --- |
| 打开一个页面 | ✅ | 可选（api 内部调路由） |
| 播放音乐、查未读数 | ❌ | ✅ |
| 模块间传复杂回调 | 用 Navigation 返回结果 API | ✅ 接口回调 |
| 外部 Deep Link 唤起 | ✅ `nav_graph` 配置 `<deepLink>` | — |

### 5.12 导航相关禁止事项

| ❌ 禁止 | ✅ 正确做法 |
| --- | --- |
| `business-A` 依赖 `business-B` 只为跳转 | 依赖 `api-B` 或 `support-router` |
| 各模块自己 `startActivity` 打开对方 Activity | 单 Activity + NavController |
| 在 ViewModel 里持有 `NavController` | 导航作为 Effect 发到 UI 层执行 |
| 在 `api` 模块写 Fragment 类 | `api` 只放 interface |

---

## 6. api 模块（能力接口层）

> **这里的 `api` 不是网络 HTTP 接口模块**，而是 **业务模块对外暴露能力的接口模块**。

### 6.1 作用

- 模块 A 需要调用模块 B 的能力时，只 **依赖 `api-b`（接口）**，不依赖 `module-b`（实现）。
- 实现类在各自业务模块中注册到 Hilt / 路由容器，调用方通过接口获取实例。

### 6.2 示例

```kotlin
// api/api-music/.../MusicApi.kt
interface MusicApi {
    fun play(songId: String)
    fun currentSong(): SongInfo?
}

// business/module-music 中实现并 @Binds 到 Hilt
// business/module-home 仅依赖 api-music：
// @Inject lateinit var musicApi: MusicApi
// musicApi.play("1001")
```

---

## 7. arch 架构层（核心，🟡 已部分落地）

### 7.1 真实目录结构

> 下面是**磁盘上的真实结构**（已移除文档历史版本里并不存在的 `BaseDialog`、`di/`、`config/`）。**加粗**项为本次随文档补齐、让四套架构 Activity+Fragment 对称的新增文件。

```
arch/src/main/java/com/mic/guide/arch/
├── base/
│   ├── BaseActivity.kt        # ViewBinding 生命周期 + 钩子
│   ├── BaseFragment.kt        # ViewBinding 生命周期 + 钩子
│   ├── BaseViewModel.kt       # launchWithLoading / loading(StateFlow) / error(SharedFlow)
│   ├── BaseRepository.kt      # safeCall → Result<T>
│   ├── BaseApplication.kt     # 全局 context + onInit()
│   └── FlowExt.kt             # ★ 本次新增：collectIn(owner) 生命周期安全收集
├── mvc/
│   ├── MvcActivity.kt
│   ├── MvcFragment.kt         # ★ 本次新增
│   └── MvcController.kt
├── mvp/
│   ├── MvpActivity.kt
│   ├── MvpFragment.kt
│   ├── MvpPresenter.kt        # 弱引用 View，随生命周期自动 detach
│   └── MvpView.kt             # View 契约标记接口
├── mvvm/
│   ├── MvvmActivity.kt        # 自动 collectIn loading/error
│   ├── MvvmFragment.kt        # 自动 collectIn loading/error（viewLifecycleOwner）
│   └── MvvmViewModel.kt       # = BaseViewModel（LiveDataExt 已移除，全面 Flow）
└── mvi/
    ├── MviActivity.kt
    ├── MviFragment.kt         # ★ 本次新增
    ├── MviContract.kt         # MviIntent / MviState / MviEffect
    └── MviViewModel.kt        # dispatch / handleIntent / setState / sendEffect
```

### 7.2 四套架构 × 角色对称矩阵

| 架构 | Activity 基类 | Fragment 基类 | 其他角色 |
| --- | --- | --- | --- |
| **MVC** | `MvcActivity` | `MvcFragment` ★ | `MvcController` |
| **MVP** | `MvpActivity` | `MvpFragment` | `MvpPresenter` / `MvpView` |
| **MVVM** | `MvvmActivity` | `MvvmFragment` | `MvvmViewModel`（=`BaseViewModel`） |
| **MVI** | `MviActivity` | `MviFragment` ★ | `MviViewModel<I,S,E>` / `MviContract` |

### 7.3 真实 API 速查（来自源码，照抄即可）

**`base/`**

```kotlin
abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    protected val binding: VB
    abstract fun createBinding(inflater: LayoutInflater): VB
    // 初始化顺序：createBinding → beforeInit → initView → initData → observe
    protected open fun beforeInit() {}
    protected open fun initView() {}
    protected open fun initData() {}
    protected open fun observe() {}
    protected fun toast(msg: CharSequence)
}

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    protected val binding: VB                 // 仅 onCreateView~onDestroyView 期间可用
    abstract fun createBinding(inflater: LayoutInflater, container: ViewGroup?): VB
    protected open fun beforeInit() {}
    protected open fun initView() {}
    protected open fun initData() {}
    protected open fun observe() {}
    protected fun toast(msg: CharSequence)
}

abstract class BaseViewModel : ViewModel() {
    val loading: StateFlow<Boolean>           // 持有最新值，新订阅者立即收到
    val error: SharedFlow<Throwable>          // 一次性事件（replay=0），UI 重建不重放
    protected fun launchWithLoading(          // 自动管理 loading；异常 tryEmit 到 error；CancellationException 透传
        showLoading: Boolean = true,
        block: suspend () -> Unit,
    ): Job
}

abstract class BaseRepository {
    // 在 Dispatchers.IO 执行，try/catch 包成 Result，对业务层屏蔽线程与异常细节
    protected suspend fun <T> safeCall(block: suspend () -> T): Result<T>
}

abstract class BaseApplication : Application() {
    protected abstract fun onInit()
    companion object { lateinit var instance: BaseApplication; val appContext: Context }
}

// FlowExt.kt —— 全工程统一的生命周期安全收集入口
inline fun <T> Flow<T>.collectIn(
    owner: LifecycleOwner,
    activeState: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend (T) -> Unit,
)   // 内部 owner.lifecycleScope + repeatOnLifecycle；Fragment 传 viewLifecycleOwner
```

**`mvvm/`**

```kotlin
abstract class MvvmViewModel : BaseViewModel()   // 纯子类，无新增抽象

// Activity / Fragment 需提供 viewModel，基类在 beforeInit 自动 collectIn loading/error
abstract class MvvmActivity<VB : ViewBinding, VM : BaseViewModel> : BaseActivity<VB>() {
    protected abstract val viewModel: VM         // 建议 by viewModels()
    protected open fun onLoading(loading: Boolean) {}
    protected open fun onError(e: Throwable) { /* 默认 toast */ }
}
abstract class MvvmFragment<VB : ViewBinding, VM : BaseViewModel> : BaseFragment<VB>() {
    protected abstract val viewModel: VM
    protected open fun onLoading(loading: Boolean) {}
    protected open fun onError(e: Throwable) { /* 默认 toast */ }
}
```

**`mvi/`**

```kotlin
interface MviIntent     // 用户意图标记
interface MviState      // UI 状态标记（应为不可变 data class）
interface MviEffect     // 一次性副作用标记（Toast / 导航 / Dialog）

abstract class MviViewModel<I : MviIntent, S : MviState, E : MviEffect>(
    initialState: S,
) : BaseViewModel() {
    val state: StateFlow<S>
    val effect: Flow<E>
    protected val currentState: S
    fun dispatch(intent: I)                      // 外部入口
    protected abstract fun handleIntent(intent: I)
    protected fun setState(reducer: S.() -> S)
    protected fun sendEffect(effect: E)
}

// Activity / Fragment 在 STARTED 周期内收集 state/effect
abstract class MviActivity<VB, I, S, E, VM : MviViewModel<I, S, E>> : BaseActivity<VB>() {
    protected abstract val viewModel: VM
    protected abstract fun renderState(state: S)
    protected abstract fun handleEffect(effect: E)
}
abstract class MviFragment<VB, I, S, E, VM : MviViewModel<I, S, E>> : BaseFragment<VB>() {
    protected abstract val viewModel: VM
    protected abstract fun renderState(state: S)
    protected abstract fun handleEffect(effect: E)
}
```

**`mvc/` 与 `mvp/`**

```kotlin
abstract class MvcController(protected val context: Context) {
    open fun onCreate() {}
    open fun onDestroy() {}
}
abstract class MvcActivity<VB, C : MvcController> : BaseActivity<VB>() {
    protected lateinit var controller: C
    abstract fun createController(): C           // beforeInit 创建，onDestroy 释放
}
abstract class MvcFragment<VB, C : MvcController> : BaseFragment<VB>() {
    protected lateinit var controller: C
    abstract fun createController(): C           // beforeInit 创建，onDestroyView 释放
}

interface MvpView                                // 空标记接口
abstract class MvpPresenter<V : MvpView> : DefaultLifecycleObserver {
    protected val view: V?                       // 弱引用，防泄漏
    fun attach(view: V); fun detach()            // onDestroy 自动 detach
}
abstract class MvpActivity<VB, V : MvpView, P : MvpPresenter<V>> : BaseActivity<VB>()
abstract class MvpFragment<VB, V : MvpView, P : MvpPresenter<V>> : BaseFragment<VB>()
```

### 7.4 arch 模块依赖（真实 `arch/build.gradle.kts`）

```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
}
android {
    namespace = "com.mic.guide.arch"
    buildFeatures { viewBinding = true }         // 泛型 ViewBinding 基类必需
}
dependencies {
    api(libs.androidx.core.ktx)
    api(libs.androidx.appcompat)
    api(libs.androidx.material)
    api(libs.androidx.activity.ktx)
    api(libs.androidx.fragment)
    api(libs.androidx.lifecycle.runtime.ktx)     // repeatOnLifecycle / lifecycleScope
    api(libs.androidx.lifecycle.viewmodel.ktx)
    api(libs.androidx.lifecycle.livedata)        // 可选：arch 已全面 Flow，保留以便业务按需用 LiveData
    api(libs.kotlinx.coroutines.android)         // StateFlow / SharedFlow / collectIn 的基础
    testImplementation(libs.junit)
}
```

> ❗ **arch 未引入 Hilt**。本工程统一以 Hilt 为 DI 方案（版本目录已声明 2.57.2），但 DI 接入点在各 `business`/`support` 模块，**不在 arch**。这样 arch 保持轻量、与 DI 框架解耦。

### 7.5 四种架构适用场景

| 架构 | 适用场景 |
| --- | --- |
| **MVC** | 极简页面 / 遗留代码兼容 |
| **MVP** | 逻辑较重、需明确 View 契约的页面 |
| **MVVM** | **默认主力**，绝大多数业务页面 |
| **MVI** | 状态复杂、需单向数据流；导航/Toast 走 Effect |

---

## 8. business 业务模块标准结构（规划 ⬜）

```
business/module-home/
├── src/main/
│   ├── java/com/mic/guide/module/home/
│   │   ├── ui/              # Fragment / ViewModel / Adapter
│   │   ├── data/
│   │   │   ├── remote/      # ApiService + DTO
│   │   │   ├── local/       # Room / DataStore
│   │   │   ├── mapper/
│   │   │   └── repository/
│   │   ├── domain/          # 可选：Model / UseCase
│   │   └── di/              # Hilt Module
│   └── res/
│       └── navigation/      # 本子模块 NavGraph
├── build.gradle.kts
└── README.md
```

**规则：**
- 页面继承 `arch` 对应 Base 类，优先 MVVM / MVI；逻辑写在 `initView()` / `observe()` 钩子，VM 用 `by viewModels()`。
- 跨业务调用走 `api`；跨业务**页面**跳转走 `support-router`。
- 不得 `implementation(project(":business:module-xxx"))`。
- **每个 business 模块自带 Hilt**（arch 不传递）：在 `build.gradle.kts` 加 Hilt 插件 + `hilt.android` + `kapt(hilt.compiler)`。

---

## 9. support 支撑模块（规划 ⬜）

| 模块 | 职责 | 关键技术 |
| --- | --- | --- |
| `support-network` | HTTP **基础设施** | Retrofit 2.9.0 + OkHttp 4.11.0 + 拦截器 |
| `support-websocket` | WebSocket 长连接 | OkHttp WebSocket |
| `support-router` 🟡 | **Navigation 门面**（已落地）、Route 常量、deepLink 降级 | Jetpack Navigation 2.5.1 |
| `support-storage` | 轻量 KV | DataStore 1.0.0 |
| `support-database` | 结构化持久化 | Room 2.4.0 |
| `support-permission` | 运行时权限 | ActivityResult API |

> 建议：本工程已声明的硬件/脚本能力（BLE、串口、CameraX、Chaquopy/Python）应各自独立成 `support-ble` / `support-serial` / `support-camera` / `support-python` 等模块，沿用相同分层规范，避免渗入业务层。

---

## 10. 端到端数据流总览

以「首页点击会话 → 进入聊天详情 → 拉取消息列表」为例：

```
1. 用户点击会话项
   HomeFragment → HomeMviViewModel.dispatch(OpenChat(id))
                → handleIntent → sendEffect(HomeEffect.NavigateToChat(id))
                → HomeFragment.handleEffect → AppNavigator.toChatDetail(id)

2. Navigation 打开 ChatDetailFragment（module-chat）
   传入 argument: conversationId

3. ChatDetailViewModel 初始化
   → launchWithLoading { ChatRepository.loadMessages(conversationId) }
   → ChatRepository.safeCall { ChatApiService.getMessages() }   // Retrofit
   → support-network OkHttp 发请求
   → Result 回传 → setState / StateFlow 更新 UI（异常自动进 error: SharedFlow）
```

---

## 11. 依赖规则与模块通信

### 11.1 依赖引入方式（规划 ⬜）

```kotlin
// business/module-home/build.gradle.kts
dependencies {
    api(project(":arch"))
    implementation(project(":api:api-chat"))
    implementation(project(":api:api-music"))
    implementation(project(":support:support-network"))
    implementation(project(":support:support-router"))
    implementation(project(":libs:lib-ui"))
    // Hilt（arch 不传递）
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
}

// app/build.gradle.kts — 唯一可以依赖所有 business 模块的地方
dependencies {
    implementation(project(":business:module-home"))
    implementation(project(":business:module-chat"))
    // ...
    implementation(project(":support:support-router"))
    implementation(libs.androidx.navigation.fragment)
}
```

### 11.2 模块间通信三种方式

1. **能力接口（api）**：非 UI 能力（播放、配置、未读数）。
2. **路由（support-router + Navigation）**：跨模块页面跳转。
3. **事件总线**：跨模块解耦事件，推荐 **`SharedFlow`**（替代 EventBus）。若需现成方案，版本目录已声明 `LiveEventBus 1.8.0` 可直接引。

> 以上三种通信方式正是「业务模块彼此零编译依赖」的基础。让模块**可随意拔插**（集成/组件双模式 + 自动注册）的完整方案见 **§15**。

---

## 12. 开发与构建规范

- **版本统一管理**：`gradle/libs.versions.toml`（Version Catalog）；冲突依赖在根 `build.gradle.kts` 用 `resolutionStrategy.force` 固定。
- **JDK 工具链**：根脚本对所有子模块 `jvmToolchain(21)`，与启动构建的 JVM 解耦（避免「无效的源发行版：21」）。
- **架构选型**：简单页面 → MVVM；复杂状态 → MVI。
- **页面继承**：必须继承 `arch` Base 类，禁止裸写 `AppCompatActivity` / `Fragment`；逻辑写在 `initView()/observe()` 钩子，不要绕过基类直接覆写 `onCreate/onViewCreated`。
- **网络**：ViewModel → Repository → ApiService，禁止跳层。
- **导航**：单 Activity + NavHost；跨模块经 `AppNavigator` 或 `api`。
- **DI**：统一 Hilt，接入点在 business/support 模块（arch 不含 Hilt）。
- **状态与数据流（协程 + Flow）**：UI 状态用 `StateFlow`，一次性事件（导航/Toast/错误）用 `SharedFlow` / `Channel`；页面一律 `collectIn(viewLifecycleOwner)` 在 `STARTED` 收集，禁止裸 `lifecycleScope.launch { flow.collect }` 收集（易泄漏/后台空转）。Repository 流式数据（Room/DataStore）直接返回 `Flow`，一次性请求用 `safeCall → Result`。
- **构建收敛**：模块数量增多后用 `build-logic` 约定插件统一构建脚本（见 §2.3）。
- **命名规范**：包名 `com.mic.guide.<层>.<模块>`；路由 URI `模块/页面/{参数}`。

---

## 13. 落地任务清单（据实校正）

- [x] 更新 `settings.gradle.kts`，`include` 全部模块 ⚠️ **但 21 个模块目录未建，当前 sync 失败**（见 §14）
- [x] 生成 `arch` 基础代码：`BaseActivity` / `BaseFragment` / `BaseViewModel` / `BaseRepository` / `BaseApplication`（备注：**无 `di/` `config/` `BaseDialog`**）
- [x] 生成 MVC / MVP / MVVM / MVI 四套架构模板基类（本次补齐 `MvcFragment` / `MviFragment`，四套 Activity+Fragment 已对称）
- [x] arch 全面切换协程 + Flow：`loading: StateFlow`、`error: SharedFlow`、新增 `FlowExt.collectIn`，移除 `LiveDataExt`
- [ ] 为 21 个已声明模块创建目录 + 最小 `build.gradle.kts`，让工程可 sync（§14）
- [ ] 引入 `build-logic` 约定插件，收敛各模块构建脚本（§2.3）
- [ ] 按工程性质补充扩展模块（§2 树中 ➕ 项，依赖与约束见 §2.2）：优先 `support-ai`，再按 ★ 推进 `support-media/ble/serial/camera/python`、`lib-widget/lib-test`、`common`、`baseline-profile`
- [ ] 版本目录补声明 **Safe Args 插件**；根脚本启用
- [ ] 搭建 `support-network` 骨架（NetworkClient / ApiResponse / 拦截器）
- [x] 搭建 `support-router`（`Routes` / `AppNavigator` 门面 / `NavigatorProvider` / `navigateSafe` 降级），`app` 注册门面、`module-home` 经门面跨模块跳 chat，**已真机验证（含降级）**
- [ ] `app` 改造：`MainActivity` + NavHost + 主 NavGraph + BottomNavigation 绑定
- [ ] 在 business/support 模块接入 **Hilt**（arch 不含）
- [ ] 为每个 `business` 模块创建标准目录与 `README.md`
- [ ] 定义各 `api-xxx` 能力接口并接入 Hilt
- [x] 打通 `module-home` MVVM 端到端示范：`HomeRepository(safeCall)` → `HomeViewModel(StateFlow)` → `HomeFragment(collectIn)` → `home_nav_graph`（list→detail 传参），`app` 单 Activity + NavHost 汇总子图，**已在真机跑通**（暂用内存假数据，未接 Hilt/网络）
- [x] 打通 `home → chat` **跨模块 deepLink 导航**：`module-chat` 在 `chat_nav_graph` 声明 `<deepLink aiguide://chat/detail/{conversationId}>`，`module-home` 仅用 URI 字符串 `navigate(...toUri())` 跳入，**`module-home` 零依赖 `module-chat`**（真机验证：`conversationId` 跨模块传参成功），印证 §15.1 支柱②；**待办**：接 `support-network` 真实数据
- [x] 业务模块接入组件化双模式（§15）：5 个 `module-*` 骨架 + `runAlone` 开关内联切 `application`/`library` + `src/runalone` 独立入口 + `ComponentApplication`/SPI 自注册 + `GuideApp` 的 `ServiceLoader` 装配（已过 `:app:assembleDebug`）；**待办**：收敛进 `build-logic` 约定插件

---

## 14. 落地修复步骤（让工程能 sync / 编译）

当前 `settings.gradle.kts` 声明了 23 个模块，但磁盘只有 `:app`、`:arch`。**先解决可同步性，再逐步落地。**

**方案 A：临时只保留已存在模块（最快）**
在 `settings.gradle.kts` 中注释掉尚未建目录的 `include(...)`，仅保留 `:app` 与 `:arch`，立即可 sync / 编译；之后建一个模块、放开一行。

**方案 B：为每个已声明模块补最小骨架（推进落地）**
为每个模块建：
1. 目录 + `build.gradle.kts`（library 用 `alias(libs.plugins.android.library)` + `alias(libs.plugins.jetbrains.kotlin.android)`，设 `namespace`、`compileSdk`/`minSdk` 取版本目录）；
2. `src/main/AndroidManifest.xml`（可只含空 `<manifest>`）；
3. 需要 DI 的模块加 Hilt 插件与依赖。

**推荐落地顺序**

```
libs-*  →  support-network / support-router  →  arch 接 Hilt（或在 business 接）
       →  打通一条业务线 module-home  →  api-*  →  其余 business / support
```

**配套需补的全局声明**
- 版本目录 `[plugins]`：补 `androidx.navigation.safeargs.kotlin`（当前缺）。
- 根 `build.gradle.kts`：按需 `apply false` 声明上述插件。
- Hilt：在使用 DI 的模块逐个引 `libs.hilt.android` + `kapt(libs.hilt.compiler)`，并给 `Application`、入口 Activity 加注解（`@HiltAndroidApp` / `@AndroidEntryPoint`）。

---

## 15. 组件化：模块可插拔（集成 / 组件双模式）

> 本节回答「怎么做组件化，让 `business/*` 模块可随意拔插」。本方案**不引 ARouter**（理由见 `docs/02-navigation-vs-arouter.md`），全部用官方 API（Gradle 双模式 + `ServiceLoader`），不加第三方路由总线、不加额外 APT。
>
> **落地状态（🟡 已部分实现）**：5 个 `business/module-*` 骨架、双模式开关、`ComponentApplication` + SPI 自注册、`GuideApp` 的 `ServiceLoader` 装配**均已落地并通过 `:app:assembleDebug`**。当前双模式逻辑**内联在各模块 `build.gradle.kts`**（见 §15.3）；收敛进 `build-logic` 约定插件是后续优化项（§2.3 / §13），非阻塞。

### 15.1 「随意拔插」要解决什么：三根支柱

目标：业务模块**可独立编译运行、可被壳工程随意增删、彼此零编译依赖**。把它拆成三件必须同时成立的事：

| 支柱 | 解决的问题 | 本工程方案 | 章节 |
| --- | --- | --- | --- |
| ① 依赖可切换 | 模块平时是 `library`（被集成），开发期能当 `application` 单独跑 | `gradle.properties` 开关 + 模块脚本内联切插件（后续可收敛进 `build-logic`） | §15.2 / §15.3 / §15.4 |
| ② 通信解耦 | A 用 B 的页面/能力时**不依赖 B 的实现类**，删 B 不会让 A 编译失败 | 跨页跳转走 deepLink URI、跨模块能力走 `api-*` + Hilt、事件走 `SharedFlow` | §5 / §6 / §11（**复用，不重写**） |
| ③ 生命周期自注册 | 各模块自带初始化逻辑，`app` 增删模块时**不改 app 代码** | `arch` 定义 `ComponentApplication`，`ServiceLoader` 运行期自动发现 | §15.5 |

> 三根支柱缺一不可：只做 ① 仍是「集成时硬编码依赖」；只做 ② 而初始化写死在 `app` 里，删模块还得改 `app.onCreate`。**③ 是「真正零 app 改动」的关键**。

### 15.2 集成模式 vs 组件模式

| 维度 | 集成模式（默认） | 组件模式（独立运行） |
| --- | --- | --- |
| 模块插件 | `com.android.library` | `com.android.application` |
| 谁来组装 | 壳工程 `app` 集成全部业务模块 | 模块自己就是一个可安装 App |
| 启动入口 | 无（`app` 的 `MainActivity` 唯一入口） | 模块 `src/runalone` 提供独立 LAUNCHER |
| 用途 | 打正式包、联调全量 | 单模块快速编译 / 独立调试本模块 |
| 开关 | `runAlone.xxx=false` | `runAlone.xxx=true` |

`gradle.properties` 放总开关与每模块开关：

```properties
# 全局：是否允许业务模块进入组件(独立运行)模式（CI/正式包恒为 false）
isModulePluginMode=false
# 单模块覆盖：只让需要独立调试的模块进入组件模式
runAlone.home=false
runAlone.chat=false
```

> 约定：正式打包前确保所有 `runAlone.*=false`，避免某模块以 `application` 形态被误集成。

### 15.3 业务模块双模式构建脚本（当前实现：内联切插件）

`plugins { }` 块不能写条件，因此双模式靠**命令式 `apply(plugin = ...)`** 实现：模块脚本读 `runAlone.<key>` 开关决定套 `application` 还是 `library`，再用 `configure<BaseExtension>` 配置两种插件共有的部分。这是**当前各 `business/module-*/build.gradle.kts` 的真实写法**（已通过 `:app:assembleDebug`）：

```kotlin
// business/module-home/build.gradle.kts（真实实现，照抄即可）
import com.android.build.gradle.BaseExtension

val runAlone = (project.findProperty("runAlone.home") as String?)?.toBoolean() ?: false

if (runAlone) apply(plugin = "com.android.application")
else          apply(plugin = "com.android.library")
apply(plugin = "org.jetbrains.kotlin.android")

configure<BaseExtension> {
    namespace = "com.mic.guide.module.home"
    compileSdkVersion(libs.versions.compileSdk.get().toInt())
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        if (runAlone) {                                  // 仅组件模式才设 applicationId（library 设会报错）
            applicationId = "com.mic.guide.home.dev"     // 独立包名，可与正式包并装
            targetSdk = libs.versions.targetSdk.get().toInt()
        }
    }
    buildFeatures.viewBinding = true
    if (runAlone) {                                      // 组件模式才并入独立入口 src/runalone
        sourceSets.getByName("main") {
            manifest.srcFile("src/runalone/AndroidManifest.xml")
            java.srcDir("src/runalone/java")
        }
    }
}

dependencies { "api"(project(":arch")) }                 // 命令式 apply 后用字符串配置名
```

> 说明：① 命令式 `apply` 后 `dependencies {}` 内没有 `implementation(...)` 类型化访问器，故用 `"api"(...)`。② 编译参数（JDK21 toolchain / compileOptions）由根 `build.gradle.kts` 的 `subprojects { plugins.withId(...) }` 统一注入，模块脚本不重复写。
>
> **后续优化（蓝图 ⬜）**：模块数变多后把上述判断收敛进 §2.3 的 `build-logic` 约定插件 `aiguide.android.feature`，模块脚本即简化为 `id("aiguide.android.feature")` 一行。此为优化项，不影响当前可插拔能力。

### 15.4 组件模式独立运行入口（`src/main` vs `src/runalone` 分目录）

集成包**不能**含模块自己的 `<application>` 与 LAUNCHER，否则和壳工程冲突；独立运行又**必须**有入口。用 source set 分目录隔离（真实目录结构）：

```
business/module-home/src/
├── main/                              # 集成 + 组件都参与编译
│   ├── AndroidManifest.xml           # 空壳 <manifest/>：无 <application>、无 LAUNCHER
│   ├── java/com/mic/guide/module/home/HomeComponent.kt      # ComponentApplication 实现（§15.5）
│   └── resources/META-INF/services/...ComponentApplication # SPI 注册文件
└── runalone/                          # 仅当 runAlone.home=true 时由 §15.3 脚本并入编译
    ├── AndroidManifest.xml           # 含 <application android:name=".HomeDebugApp"> + 带 LAUNCHER 的 Activity
    └── java/com/mic/guide/module/home/
        ├── HomeDebugApp.kt           # 继承 BaseApplication，单独跑一遍 ServiceLoader 装配
        └── HomeDebugActivity.kt      # 独立入口占位页（替换为真实首页 Fragment）
```

```xml
<!-- business/module-home/src/runalone/AndroidManifest.xml（独立运行入口） -->
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:name=".HomeDebugApp"
        android:label="HomeDev"
        android:theme="@style/Theme.AppCompat.Light.NoActionBar">
        <activity android:name=".HomeDebugActivity" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

> ⚠️ **组件化最易踩的坑 —— 为什么用 `src/runalone` 而不是 `src/debug`**：`debug` 是 Android **内置 build-type** source set，`src/debug/` 会在 **library 的 debug 构建里被自动合并**——它的 `<application>`/LAUNCHER 会被合进宿主 App，污染集成包。改用非 build-type 的 `src/runalone/`，并**只在 `runAlone=true` 时由脚本显式 `srcFile`/`srcDir` 并入**（§15.3），集成模式下它完全不参与编译，从根上杜绝泄漏。`src/main/AndroidManifest.xml` 保持空壳是另一道保险。

### 15.5 组件生命周期自注册（ServiceLoader / SPI）——真正可插拔的关键

模块各自有初始化逻辑（注册 `api-*` 实现、预热、声明 deepLink 路由），但 `app` **不应** `import` 任何模块的初始化类——否则拔模块就要改 `app`。用 Java SPI（`ServiceLoader`）让 `app` 运行期自动发现：

```kotlin
// arch/base/ComponentApplication.kt —— 组件生命周期契约（放 arch，复用 BaseApplication.onInit）
interface ComponentApplication {
    fun onCreate(app: Application)   // 模块自治初始化：注册 api 实现 / 预热 / 路由表
    fun priority(): Int = 0          // 多组件初始化顺序（大者先）
}
```

```kotlin
// business/module-home/.../HomeComponent.kt —— 模块自己的实现
class HomeComponent : ComponentApplication {
    override fun onCreate(app: Application) { /* 初始化 home 模块 */ }
    override fun priority(): Int = 10
}
```

```
# business/module-home/src/main/resources/META-INF/services/com.mic.guide.arch.base.ComponentApplication
# 文件内容仅一行（SPI 注册，无需 app 感知）：
com.mic.guide.module.home.HomeComponent
```

```kotlin
// app/.../GuideApp.kt —— 壳工程零硬编码装配（在 BaseApplication.onInit() 里调用）
override fun onInit() {
    ServiceLoader.load(ComponentApplication::class.java)
        .sortedByDescending { it.priority() }
        .forEach { it.onCreate(this) }
}
```

> **这一步就是「随意拔插」**：删一个模块 = 删 `app/build.gradle.kts` 里那一行 `implementation(project(...))`，`ServiceLoader` 自动少加载一个组件，`app` 代码**零改动**。
>
> **备选（Hilt 多绑定）**：若想要编译期类型安全，可改用 Hilt `@IntoSet` 收集 `Set<ComponentApplication>`。但代价是 `app` 必须**编译期依赖各模块**才能聚合，拔模块仍要改 `app` 依赖——可插拔性弱于 SPI。两者按「类型安全 vs 零改动」权衡，本工程默认 SPI。

### 15.6 「拔插」操作清单（加 / 减一个模块改哪里）

| 操作 | 需要改动的位置 | 说明 |
| --- | --- | --- |
| **加模块** | ① 建模块骨架（§14）<br>② `settings.gradle.kts` 加 `include(":business:module-x")`<br>③ `app/build.gradle.kts` 加一行 `implementation(project(":business:module-x"))`<br>④ 模块内写 `XComponent` + SPI 文件 + deepLink 子图 | ④ 全在模块自己内部，不碰其他模块 |
| **减模块** | 只删 ② ③ 两行 | 其余 business / `app` 代码不受影响——因为跨模块通信走 deepLink/api，编译期无目标类引用 |

> 「减只删两行」成立的铁律前提：**业务模块之间从不 `implementation(project(":business:module-x"))`**（§3 / §11 已立）。一旦某业务直接 `project` 依赖另一业务，可插拔性立即破裂。

### 15.7 组件化禁止事项

| ❌ 禁止 | ✅ 正确做法 |
| --- | --- |
| `app` 里 `when(module) { ... HomeComponent() ... }` 硬编码组件初始化 | `ServiceLoader` 自动发现 `ComponentApplication`（§15.5） |
| `business-A` 直接 `implementation(project(":business:module-b"))` | deepLink 跳转 + `api-b` 能力接口（§5 / §6） |
| 独立运行入口（LAUNCHER / `<application>`）写进 `src/main` 或 `src/debug` | 放非 build-type 的 `src/runalone`，仅 `runAlone=true` 时并入（§15.4） |
| 各模块初始化逻辑堆在 `app.onCreate()` | 各模块 `ComponentApplication.onCreate()` 自治（§15.5） |
| 正式打包时残留 `runAlone.xxx=true` | 打包前统一 `runAlone.*=false`（§15.2） |
