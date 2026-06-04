# Android 多模块项目结构与架构设计

> 工程名：**AndroidJetpack** ｜ 根包名：**`com.mic.guide`** ｜ 构建脚本：**Kotlin DSL（`.gradle.kts`）**
>
> ⚠️ 当前 `settings.gradle.kts` 仅 `include(":app")`，本文档描述的是**目标拆分蓝图**，作为后续逐步落地各模块的依据。

---

## 1. 项目概述

这是一个采用 **多模块化（Modularization）** 架构的 Android 项目，核心目标是实现 **高内聚、低耦合**，便于团队并行开发、独立编译和长期维护。

项目从职责上分为六层：

| 层级 | 模块 | 定位 |
| --- | --- | --- |
| 壳工程 | `app` | 组装入口，不写业务 |
| 接口层 | `api` | 业务对外**能力接口**，模块间解耦 |
| 架构层 | `arch` | MVC / MVP / MVVM / MVI 基础框架与 Base 类 |
| 业务层 | `business` | 各业务模块，只关心自身业务 |
| 支撑层 | `support` | 网络、存储、数据库、路由等垂直能力 |
| 工具层 | `libs` | 通用工具、UI、日志、图片、扩展 |

### 关键设计原则

1. **业务模块之间禁止直接依赖**，只能通过 `api`（能力接口）+ 路由 / 事件总线通信。
2. **`api` 是能力暴露层，不是 HTTP 网络接口**（HTTP/Retrofit 在 `support-network`）。
3. **所有页面必须继承 `arch` 中对应架构的 Base 类**，统一生命周期、Loading、异常处理等。
4. **依赖方向单向向下**：`business → api / arch / support / libs`，反向不允许。
5. 壳工程 `app` 只做模块组装、初始化、路由注册，不写具体业务。

---

## 2. 整体目录结构

```
AndroidJetpack/ (根目录)
├── build.gradle.kts
├── settings.gradle.kts
├── docs/                       # 项目文档
│   └── 01-arch.md              # 本文档
│
├── app/                        # 壳工程：组装 / 初始化 / 路由注册
│
├── api/                        # 业务对外能力接口（解耦用，非 HTTP）
│   ├── api-player/             # 播放器对外能力
│   ├── api-chat/               # 聊天对外能力
│   ├── api-music/              # 音乐对外能力
│   └── api-settings/           # 设置对外能力
│
├── arch/                       # 架构基础库（核心）
│
├── business/                   # 业务模块
│   ├── module-home/
│   ├── module-chat/
│   ├── module-music/
│   ├── module-video/
│   └── module-settings/
│
├── support/                    # 支撑模块（垂直能力）
│   ├── support-network/        # HTTP / Retrofit / OkHttp
│   ├── support-websocket/      # WebSocket 长连接
│   ├── support-router/         # 页面路由
│   ├── support-storage/        # 本地存储
│   ├── support-database/       # 数据库
│   └── support-permission/     # 权限
│
├── libs/                       # 通用工具库
│   ├── lib-common/             # 通用工具
│   ├── lib-ui/                 # 通用 UI
│   ├── lib-log/                # 日志
│   ├── lib-image/              # 图片加载
│   └── lib-extension/          # Kotlin 扩展
│
└── common/                     # 可选：公共资源（colors / strings / theme）
```

---

## 3. 模块职责说明

| 模块 | 职责 | 可依赖 |
| --- | --- | --- |
| `app` | 模块组装、`Application` 初始化、路由表注册、主题样式。**不写业务代码** | 全部 |
| `api` | 各业务模块对外暴露的能力接口（Kotlin interface），供其他模块调用而不依赖业务实现 | `arch`、`libs` |
| `arch` | 架构核心：Base 类 + MVC/MVP/MVVM/MVI 模板 + 路由/DI/工具封装 | `libs`、`support`（基础部分） |
| `business/*` | 纯业务模块，只关注自身业务逻辑 | `api`、`arch`、`support`、`libs` |
| `support/*` | 垂直支撑能力（网络、存储、数据库、权限、路由、长连接） | `libs` |
| `libs/*` | 与业务无关的通用工具、UI、扩展 | 仅彼此基础依赖 |

**依赖方向（单向向下）：**

```
        app
         │  组装
   ┌─────┼───────────────┐
business  api            （business 之间不可互相依赖）
   │   ┌──┘
   ▼   ▼
  arch
   │
   ▼
support ──► libs
```

---

## 4. api 模块（能力接口层）—— 重点澄清

> **这里的 `api` 不是网络 HTTP 接口模块**，而是 **业务模块对外暴露能力的接口模块**，用于解耦业务模块之间的直接依赖。

### 作用

- 模块 A 需要调用模块 B 的能力时，只 **依赖 `api-b`（接口）**，不依赖 `module-b`（实现）。
- 实现类在各自业务模块中注册到路由 / DI 容器，调用方通过接口 + 路由获取实例。

### 子模块

```
api/
├── api-player/      # 播放器对外能力（如 play / pause / 当前进度查询）
├── api-chat/        # 聊天对外能力（如 跳转会话 / 未读数）
├── api-music/       # 音乐对外能力（如 播放指定歌单）
└── api-settings/    # 设置对外能力（如 读取主题 / 语言配置）
```

### 示例

```kotlin
// api/api-music/.../MusicApi.kt
interface MusicApi {
    fun play(songId: String)
    fun currentSong(): SongInfo?
}

// business/module-music 中实现并注册；module-home 仅依赖 api-music 即可调用：
// val music = Router.get(MusicApi::class.java)
// music?.play("1001")
```

---

## 5. arch 架构层（核心）

```
arch/
└── src/main/java/com/mic/guide/arch/
    ├── base/                    # 所有架构共用的基础类
    │   ├── BaseActivity.kt
    │   ├── BaseFragment.kt
    │   ├── BaseViewModel.kt
    │   ├── BaseRepository.kt
    │   ├── BaseApplication.kt
    │   └── BaseDialog.kt
    │
    ├── mvc/                     # MVC 架构模板
    │   ├── MvcActivity.kt
    │   └── MvcController.kt
    │
    ├── mvp/                     # MVP 架构模板
    │   ├── MvpActivity.kt
    │   ├── MvpFragment.kt
    │   ├── MvpPresenter.kt      # Presenter 基类（持有 View 弱引用）
    │   └── MvpView.kt           # View 契约接口
    │
    ├── mvvm/                    # MVVM 架构模板（主力推荐）
    │   ├── MvvmActivity.kt
    │   ├── MvvmFragment.kt
    │   ├── MvvmViewModel.kt
    │   └── extensions/          # LiveData / Flow 扩展
    │
    ├── mvi/                     # MVI 架构模板（复杂状态页面推荐）
    │   ├── MviActivity.kt
    │   ├── MviViewModel.kt      # 单向数据流：Intent → reduce → State
    │   ├── MviIntent.kt         # 用户意图
    │   ├── MviState.kt          # 不可变 UI 状态
    │   └── MviEffect.kt         # 一次性副作用（Toast / 导航）
    │
    ├── router/                  # 统一路由封装（ARouter / Navigation）
    ├── di/                      # 依赖注入配置（Hilt / Koin）
    ├── utils/                   # 架构专用工具
    └── config/                  # 全局配置（主题、全局异常处理器等）
```

### base 核心类功能要点

| 基类 | 职责 |
| --- | --- |
| `BaseActivity` / `BaseFragment` | 统一生命周期、权限申请、Loading 对话框、Toast、状态栏沉浸、ViewBinding、事件总线注册 |
| `BaseViewModel` | Loading 状态封装、统一错误处理、协程作用域（`viewModelScope`）、页面结果回调 |
| `BaseRepository` | 抽象本地 + 网络数据源切换逻辑、统一异常包装 |
| `BaseApplication` | 模块初始化入口、全局 Context、路由 / DI 容器启动 |
| `BaseDialog` | 通用对话框基类（样式、动画、生命周期感知） |

### 四种架构适用场景

| 架构 | 适用场景 |
| --- | --- |
| **MVC** | 极简页面 / 遗留代码兼容 |
| **MVP** | 逻辑较重、需明确 View 契约的页面；遗留项目迁移过渡 |
| **MVVM** | **默认主力**，绝大多数业务页面 |
| **MVI** | 状态复杂、需要单向数据流与可预测状态的页面 |

---

## 6. business 业务模块

### 业务模块列表

```
business/
├── module-home/        # 首页
├── module-chat/        # 聊天
├── module-music/       # 音乐
├── module-video/       # 视频
└── module-settings/    # 设置
```

### 标准模块内部结构（以 module-home 为例）

```
business/module-home/
├── src/main/java/com/mic/guide/module/home/
│   ├── ui/              # 页面与视图（Activity / Fragment / Adapter）
│   ├── data/            # 数据仓库（依赖 api 与 support）
│   ├── domain/          # 业务逻辑层 / UseCase（可选）
│   ├── di/              # 模块内依赖注入
│   └── navigation/      # 本模块路由表
├── build.gradle.kts
└── README.md            # 模块职责、依赖、使用说明
```

**规则：**
- 页面继承 `arch` 对应架构的 Base 类，优先 MVVM / MVI。
- 跨业务调用走 `api`：`implementation(project(":api:api-music"))`。
- 不得出现 `implementation(project(":business:module-xxx"))` 这种业务互依赖。

---

## 7. support 支撑模块

| 模块 | 职责 | 关键技术 |
| --- | --- | --- |
| `support-network` | **HTTP / HTTPS 网络请求**、拦截器、统一返回封装 | Retrofit + OkHttp |
| `support-websocket` | WebSocket 长连接、心跳、断线重连 | OkHttp WebSocket |
| `support-router` | 页面路由、跨模块跳转、参数传递 | ARouter / Navigation |
| `support-storage` | 轻量本地存储（KV） | DataStore / SharedPreferences |
| `support-database` | 结构化数据持久化 | Room / SQLite |
| `support-permission` | 运行时权限申请与回调封装 | ActivityResult API |

---

## 8. libs 工具库

| 模块 | 职责 |
| --- | --- |
| `lib-common` | 通用工具类（时间、加密、设备信息、线程等） |
| `lib-ui` | 通用 UI 组件（自定义 View、空/错误/加载状态视图） |
| `lib-log` | 统一日志框架（分级、落盘、上报） |
| `lib-image` | 图片加载封装（Glide / Coil） |
| `lib-extension` | Kotlin 扩展函数（View / Context / Flow 等） |

---

## 9. 依赖规则与模块通信

### 依赖引入方式

```kotlin
// business/module-home/build.gradle.kts
dependencies {
    api(project(":arch"))                        // 架构基类，对外传递
    implementation(project(":api:api-music"))    // 调用音乐能力
    implementation(project(":support:support-network"))
    implementation(project(":libs:lib-ui"))
}
```

- `api(...)`：需要把依赖**传递**给上层（如 `arch` 暴露给业务）。
- `implementation(...)`：仅本模块内部使用，**默认优先**，可加快编译。

### 模块间通信三种方式

1. **能力接口（api）**：调用方依赖 `api-xxx`，通过路由/DI 获取实现。
2. **路由跳转**：`support-router` 统一管理页面跳转与参数。
3. **事件总线**：跨模块解耦事件，推荐 `SharedFlow`（替代 EventBus）。

---

## 10. 开发与构建规范

- **版本统一管理**：依赖版本集中在根 `build.gradle.kts` / `gradle/libs.versions.toml`（Version Catalog）。
- **架构选型**：简单页面 → MVVM；复杂状态 → MVI；遗留代码 → 逐步迁移至 MVP/MVVM。
- **页面继承**：所有页面必须继承 `arch` 对应 Base 类，禁止裸写 `AppCompatActivity`。
- **模块文档**：每个模块根目录需有 `README.md`，说明职责、依赖与对外能力。
- **编译优化**：模块独立编译，CI/CD 仅构建变更模块。
- **命名规范**：模块前缀清晰（`api-` / `module-` / `support-` / `lib-`），包名以 `com.mic.guide.<层>.<模块>` 组织。

---

## 11. 后续可执行任务清单

> 本文档作为蓝图，后续可让 Agent 按需逐项落地：

- [ ] 更新 `settings.gradle.kts`，`include` 全部模块
- [ ] 生成各模块 `build.gradle.kts` 与依赖配置
- [ ] 生成 `arch` 基础代码：`BaseActivity` / `BaseViewModel` / `BaseRepository` 等
- [ ] 生成 MVC / MVP / MVVM / MVI 四套架构模板基类
- [ ] 为每个 `business` 模块创建标准目录与 `README.md`
- [ ] 定义各 `api-xxx` 能力接口并接入路由
- [ ] 搭建 `support-network` / `support-router` 等支撑模块骨架
