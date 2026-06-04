# Android 多模块架构说明

## 1. 文档目的

本文档用于说明当前 Android 项目的模块划分、模块职责、依赖关系、命名规范以及新增模块时需要遵守的规则。

主要目标：

- 明确每个模块的职责边界
- 避免模块之间产生混乱依赖
- 降低业务模块之间的耦合
- 提升项目可维护性和可扩展性
- 方便新人快速理解项目结构
- 为后续模块拆分、组件化、插件化提供基础

---

## 2. 项目整体结构

当前项目采用多模块架构，整体分为以下几类模块：

| 类型 | 说明 |
|---|---|
| 壳工程 | 应用入口，负责启动、初始化、模块组装 |
| 业务模块 | 承载具体业务功能，例如首页、登录、订单、支付 |
| 支撑模块 | 提供业务运行所需的基础能力，例如网络、路由、存储 |
| 工具模块 | 提供通用工具、通用 UI、日志、扩展方法等 |
| API 模块 | 用于模块间能力暴露和解耦，可选但推荐 |

---

## 3. 推荐目录结构

```text
project-root/
├── app/                                # 壳工程
│
├── business/                           # 业务模块
│   ├── module-home/                    # 首页模块
│   ├── module-user/                    # 用户模块
│   ├── module-order/                   # 订单模块
│   ├── module-pay/                     # 支付模块
│   └── module-message/                 # 消息模块
│
├── support/                            # 支撑模块
│   ├── support-router/                 # 路由能力
│   ├── support-network/                # 网络能力
│   ├── support-storage/                # 本地存储能力
│   ├── support-database/               # 数据库能力
│   ├── support-analytics/              # 埋点能力
│   ├── support-permission/             # 权限能力
│   └── support-download/               # 下载能力
│
├── libs/                               # 工具模块
│   ├── lib-common/                     # 通用工具
│   ├── lib-ui/                         # 通用 UI 组件
│   ├── lib-log/                        # 日志工具
│   ├── lib-image/                      # 图片加载封装
│   ├── lib-webview/                    # WebView 封装
│   └── lib-extension/                  # Kotlin 扩展方法
│
├── api/                                # API 模块，可选但推荐
│   ├── api-user/                       # 用户模块对外接口
│   ├── api-order/                      # 订单模块对外接口
│   └── api-pay/                        # 支付模块对外接口
│
├── build-logic/                        # 构建逻辑，可选
│   ├── convention/
│   └── plugins/
│
├── docs/                               # 项目文档
│   ├── module-architecture.md          # 模块架构说明
│   ├── dependency-rules.md             # 依赖规则说明
│   └── module-template.md              # 新增模块模板
│
├── settings.gradle
├── build.gradle
└── README.md
```

---

## 4. 模块分层说明

项目整体依赖方向应该是自上而下的。

```text
app 壳工程
   ↓
业务模块 business
   ↓
支撑模块 support
   ↓
工具模块 libs
```

推荐依赖方向：

```text
app
 ├── module-home
 ├── module-user
 ├── module-order
 └── module-pay

业务模块
 ├── support-router
 ├── support-network
 ├── support-storage
 ├── support-analytics
 └── libs

支撑模块
 └── libs

工具模块
 └── 不依赖业务模块
```

禁止依赖方向：

```text
libs        ❌ 不允许依赖 business
support     ❌ 不允许依赖 business
business A  ❌ 不建议直接依赖 business B
business    ❌ 不允许依赖 app
```

---

## 5. 壳工程说明

### 5.1 模块名称

```text
app
```

### 5.2 模块职责

`app` 是应用的主壳工程，主要负责应用级别的组装和启动。

主要职责：

- 提供应用入口
- 配置 `Application`
- 初始化全局 SDK
- 组装业务模块
- 配置启动页
- 配置全局主题
- 配置主导航
- 管理全局依赖注入入口
- 管理应用级别资源
- 管理打包配置

### 5.3 不应该承担的职责

`app` 不应该写复杂业务逻辑。

不推荐放在 `app` 中的内容：

- 首页具体业务逻辑
- 登录业务逻辑
- 订单业务逻辑
- 支付业务逻辑
- 网络请求实现
- 数据库实现
- 复杂 UI 组件
- 可复用工具类

错误示例：

```text
app/
├── LoginRepository.kt        ❌ 登录业务不应放在 app
├── OrderManager.kt           ❌ 订单业务不应放在 app
├── HttpClient.kt             ❌ 网络实现不应放在 app
└── CommonDialog.kt           ❌ 通用 UI 不应放在 app
```

推荐做法：

```text
module-user/
└── LoginRepository.kt

module-order/
└── OrderManager.kt

support-network/
└── HttpClient.kt

lib-ui/
└── CommonDialog.kt
```

---

## 6. 业务模块说明

业务模块用于承载具体业务功能。

### 6.1 业务模块示例

| 模块 | 说明 |
|---|---|
| module-home | 首页模块 |
| module-user | 用户模块 |
| module-order | 订单模块 |
| module-pay | 支付模块 |
| module-message | 消息模块 |
| module-search | 搜索模块 |
| module-setting | 设置模块 |

### 6.2 业务模块职责

业务模块应该包含完整的业务闭环。

以 `module-order` 为例，它可以包含：

```text
module-order/
├── ui/                         # 页面、Fragment、Compose 页面
├── viewmodel/                  # ViewModel
├── repository/                 # 数据仓库
├── datasource/                 # 远程与本地数据源
├── model/                      # 业务数据模型
├── service/                    # 模块内部服务
├── router/                     # 模块内部路由处理
└── di/                         # 依赖注入配置
```

### 6.3 业务模块内部推荐结构

```text
module-order/
├── src/main/
│   ├── java/com/example/order/
│   │   ├── ui/
│   │   │   ├── OrderListActivity.kt
│   │   │   ├── OrderDetailActivity.kt
│   │   │   └── OrderConfirmFragment.kt
│   │   │
│   │   ├── viewmodel/
│   │   │   ├── OrderListViewModel.kt
│   │   │   └── OrderDetailViewModel.kt
│   │   │
│   │   ├── repository/
│   │   │   └── OrderRepository.kt
│   │   │
│   │   ├── datasource/
│   │   │   ├── OrderRemoteDataSource.kt
│   │   │   └── OrderLocalDataSource.kt
│   │   │
│   │   ├── model/
│   │   │   ├── Order.kt
│   │   │   └── OrderDetail.kt
│   │   │
│   │   ├── api/
│   │   │   └── OrderApiService.kt
│   │   │
│   │   ├── router/
│   │   │   └── OrderRouterTable.kt
│   │   │
│   │   └── di/
│   │       └── OrderModule.kt
│   │
│   ├── res/
│   └── AndroidManifest.xml
│
└── build.gradle
```

### 6.4 业务模块依赖原则

业务模块可以依赖：

```text
support-router
support-network
support-storage
support-analytics
lib-common
lib-ui
lib-log
api-xxx
```

业务模块不建议直接依赖其他业务模块。

不推荐：

```kotlin
// module-order 直接依赖 module-user
implementation(project(":business:module-user")) // ❌
```

推荐：

```kotlin
// module-order 依赖 user 对外暴露的 API
implementation(project(":api:api-user")) // ✅
```

---

## 7. 支撑模块说明

支撑模块用于提供业务运行所需的基础能力，通常不包含具体业务逻辑。

### 7.1 支撑模块示例

| 模块 | 说明 |
|---|---|
| support-router | 路由跳转能力 |
| support-network | 网络请求能力 |
| support-storage | MMKV、DataStore、SharedPreferences 封装 |
| support-database | Room、SQLite 封装 |
| support-analytics | 埋点能力 |
| support-permission | 权限申请能力 |
| support-download | 文件下载能力 |
| support-upload | 文件上传能力 |
| support-location | 定位能力 |
| support-push | 推送能力 |

### 7.2 支撑模块职责示例

以 `support-network` 为例，它负责：

- Retrofit / OkHttp 初始化
- Header 统一处理
- Token 注入
- 网络日志
- 错误码统一处理
- 请求结果封装
- 网络状态监听
- API 基类定义

示例结构：

```text
support-network/
├── src/main/java/com/example/network/
│   ├── NetworkManager.kt
│   ├── ApiClient.kt
│   ├── ApiResult.kt
│   ├── ApiException.kt
│   ├── interceptor/
│   │   ├── HeaderInterceptor.kt
│   │   ├── TokenInterceptor.kt
│   │   └── LoggingInterceptor.kt
│   └── converter/
│       └── JsonConverter.kt
└── build.gradle
```

### 7.3 支撑模块限制

支撑模块不应该包含具体业务逻辑。

不推荐：

```text
support-network/
└── UserLoginApi.kt       ❌ 用户登录接口不应放这里

support-storage/
└── OrderCacheManager.kt  ❌ 订单缓存不应放这里
```

推荐：

```text
module-user/
└── api/UserLoginApi.kt

module-order/
└── repository/OrderCacheRepository.kt
```

支撑模块可以提供基础能力，但不关心具体业务。

---

## 8. 工具模块说明

工具模块用于提供通用、无业务属性的能力。

### 8.1 工具模块示例

| 模块 | 说明 |
|---|---|
| lib-common | 通用工具类 |
| lib-ui | 通用 UI 组件 |
| lib-log | 日志工具 |
| lib-image | 图片加载封装 |
| lib-extension | Kotlin 扩展方法 |
| lib-webview | WebView 通用封装 |
| lib-resource | 公共资源 |
| lib-theme | 公共主题 |

### 8.2 工具模块职责

工具模块应该满足以下特征：

- 可复用
- 无业务含义
- 不依赖业务模块
- 可以被多个模块使用
- 尽量保持稳定

例如 `lib-common`：

```text
lib-common/
├── DateUtils.kt
├── StringUtils.kt
├── NumberUtils.kt
├── DeviceUtils.kt
├── AppUtils.kt
└── CollectionUtils.kt
```

例如 `lib-ui`：

```text
lib-ui/
├── dialog/
│   ├── CommonDialog.kt
│   └── LoadingDialog.kt
├── view/
│   ├── EmptyView.kt
│   ├── ErrorView.kt
│   └── StateLayout.kt
└── widget/
    ├── RoundImageView.kt
    └── TitleBar.kt
```

### 8.3 工具模块限制

工具模块禁止依赖业务模块。

不推荐：

```kotlin
// lib-ui 中依赖 module-user
implementation(project(":business:module-user")) // ❌
```

不推荐：

```text
lib-common/
└── UserManager.kt       ❌ 带有用户业务含义
```

推荐：

```text
module-user/
└── UserManager.kt       ✅
```

---

## 9. API 模块说明

API 模块用于业务模块之间的解耦。

当业务模块之间需要互相调用能力时，不建议直接依赖具体业务模块，而是通过 API 模块暴露接口。

### 9.1 为什么需要 API 模块

假设订单模块需要获取用户信息。

不推荐：

```text
module-order 直接依赖 module-user
```

问题：

- 两个业务模块强耦合
- 容易产生循环依赖
- 编译速度变慢
- 后续模块拆分困难

推荐：

```text
module-user 实现 api-user
module-order 依赖 api-user
```

依赖关系：

```text
module-order  --->  api-user  <---  module-user
```

### 9.2 API 模块示例

```text
api-user/
├── UserService.kt
├── UserInfo.kt
└── UserServiceProvider.kt
```

示例代码：

```kotlin
interface UserService {
    fun isLogin(): Boolean
    fun getUserId(): String?
    fun getUserInfo(): UserInfo?
}
```

业务模块依赖：

```kotlin
implementation(project(":api:api-user"))
```

用户模块实现：

```kotlin
class UserServiceImpl : UserService {

    override fun isLogin(): Boolean {
        return UserSession.isLogin()
    }

    override fun getUserId(): String? {
        return UserSession.userId
    }

    override fun getUserInfo(): UserInfo? {
        return UserSession.userInfo
    }
}
```

订单模块使用：

```kotlin
class OrderRepository(
    private val userService: UserService
) {
    fun loadOrders() {
        val userId = userService.getUserId()
        // 根据 userId 请求订单
    }
}
```

---

## 10. 模块通信方式

模块之间常见通信方式包括：

| 方式 | 适用场景 |
|---|---|
| 路由跳转 | 页面跳转 |
| API 接口 | 获取其他模块能力 |
| 事件总线 | 低频全局事件通知 |
| 依赖注入 | 服务实现注入 |
| 公共数据层 | 极少数全局共享数据 |

### 10.1 页面跳转

推荐通过路由模块进行页面跳转。

示例：

```kotlin
Router.open("/order/detail")
    .withString("orderId", orderId)
    .navigation(context)
```

不推荐：

```kotlin
val intent = Intent(context, OrderDetailActivity::class.java)
context.startActivity(intent)
```

原因：

- 直接引用 Activity 会导致模块耦合
- 不利于模块独立编译
- 不利于后续组件化改造

### 10.2 服务调用

推荐通过 API 接口调用其他模块能力。

示例：

```kotlin
val userService = ServiceManager.get(UserService::class.java)
val userId = userService.getUserId()
```

### 10.3 事件通知

对于登录状态变化、主题变化、语言变化等全局事件，可以使用事件通知。

示例：

```kotlin
EventBus.post(LoginStateChangedEvent(isLogin = true))
```

但事件总线不建议滥用。

适合：

- 登录状态变化
- 主题切换
- 语言切换
- 全局配置变化

不适合：

- 正常业务流程传参
- 页面跳转参数传递
- 高频数据通信
- 强依赖业务调用

---

## 11. 模块依赖规范

### 11.1 总体原则

模块依赖应遵循：

```text
上层可以依赖下层
下层不能依赖上层
同层尽量不直接依赖
业务模块之间通过 api/router 通信
```

### 11.2 允许的依赖

| 当前模块 | 可以依赖 |
|---|---|
| app | business、support、libs、api |
| business | support、libs、api |
| support | libs |
| libs | 尽量只依赖第三方库或 Android 基础库 |
| api | libs 中的基础类型，尽量少依赖其他模块 |

### 11.3 不允许的依赖

| 当前模块 | 不允许依赖 |
|---|---|
| libs | app、business、support |
| support | app、business |
| business | app |
| api | business |
| business A | business B，除非经过评审 |

### 11.4 依赖示例

#### app/build.gradle

```kotlin
dependencies {
    implementation(project(":business:module-home"))
    implementation(project(":business:module-user"))
    implementation(project(":business:module-order"))
    implementation(project(":business:module-pay"))

    implementation(project(":support:support-router"))
    implementation(project(":support:support-network"))

    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-ui"))
}
```

#### module-order/build.gradle

```kotlin
dependencies {
    implementation(project(":api:api-user"))
    implementation(project(":api:api-pay"))

    implementation(project(":support:support-router"))
    implementation(project(":support:support-network"))
    implementation(project(":support:support-storage"))

    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-ui"))
}
```

#### support-network/build.gradle

```kotlin
dependencies {
    implementation(project(":libs:lib-log"))
    implementation(project(":libs:lib-common"))

    implementation("com.squareup.retrofit2:retrofit:x.x.x")
    implementation("com.squareup.okhttp3:okhttp:x.x.x")
}
```

#### lib-common/build.gradle

```kotlin
dependencies {
    implementation("androidx.core:core-ktx:x.x.x")
}
```

---

## 12. 模块命名规范

### 12.1 目录命名

| 类型 | 命名规则 | 示例 |
|---|---|---|
| 壳工程 | app | app |
| 业务模块 | module-业务名 | module-user |
| 支撑模块 | support-能力名 | support-network |
| 工具模块 | lib-能力名 | lib-common |
| API 模块 | api-业务名 | api-user |

### 12.2 包名规范

推荐包名保持和模块职责一致。

```text
com.example.app

com.example.business.home
com.example.business.user
com.example.business.order

com.example.support.network
com.example.support.router
com.example.support.storage

com.example.libs.common
com.example.libs.ui

com.example.api.user
com.example.api.order
```

### 12.3 类命名规范

| 类型 | 命名示例 |
|---|---|
| 页面 | OrderDetailActivity、OrderListFragment |
| ViewModel | OrderDetailViewModel |
| Repository | OrderRepository |
| DataSource | OrderRemoteDataSource |
| API Service | OrderApiService |
| Router | OrderRouter |
| Manager | OrderManager |
| Provider | UserServiceProvider |
| Impl | UserServiceImpl |

---

## 13. 新增模块流程

### 13.1 新增业务模块

以新增 `module-coupon` 优惠券模块为例。

第一步，创建目录：

```text
business/module-coupon/
```

第二步，在 `settings.gradle` 中注册：

```kotlin
include(":business:module-coupon")
```

第三步，配置 `build.gradle`：

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.business.coupon"
}

dependencies {
    implementation(project(":support:support-router"))
    implementation(project(":support:support-network"))
    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-ui"))
}
```

第四步，补充模块内部结构：

```text
module-coupon/
├── ui/
├── viewmodel/
├── repository/
├── datasource/
├── model/
├── router/
└── di/
```

第五步，在本文档中登记模块说明。

```md
| module-coupon | 优惠券模块 |
```

### 13.2 新增支撑模块

以新增 `support-location` 定位模块为例。

```text
support/support-location/
```

职责：

- 封装定位 SDK
- 提供统一定位接口
- 处理定位权限
- 处理定位错误
- 对业务模块隐藏具体定位实现

不允许：

- 写入附近门店业务逻辑
- 写入用户地址业务逻辑
- 写入订单配送业务逻辑

### 13.3 新增工具模块

以新增 `lib-qrcode` 二维码工具模块为例。

```text
libs/lib-qrcode/
```

职责：

- 二维码生成
- 二维码扫描基础封装
- 二维码图片处理

不允许：

- 绑定具体业务二维码类型
- 处理订单二维码业务
- 处理支付二维码业务

---

## 14. 模块独立运行说明

如果业务模块较大，可以支持独立运行。

例如：

```text
module-order/
├── src/main/
└── src/debug/
    ├── AndroidManifest.xml
    └── DebugOrderActivity.kt
```

在 `debug` 环境下提供测试入口。

示例：

```kotlin
if (isModuleDebug) {
    // 配置独立运行入口
}
```

独立运行适合：

- 大型业务模块
- 页面较多的业务模块
- 需要独立开发调试的模块
- 多人协作开发场景

不一定所有模块都需要支持独立运行。

---

## 15. 资源命名规范

为了避免多模块资源冲突，所有模块资源必须添加前缀。

### 15.1 业务模块资源前缀

| 模块 | 资源前缀 |
|---|---|
| module-home | home_ |
| module-user | user_ |
| module-order | order_ |
| module-pay | pay_ |

示例：

```text
order_activity_detail.xml
order_item_product.xml
order_ic_status_success.xml
order_bg_card.xml
```

### 15.2 支撑模块资源前缀

```text
router_
network_
storage_
analytics_
```

### 15.3 工具模块资源前缀

```text
common_
ui_
log_
image_
```

---

## 16. Manifest 规范

每个模块只声明自己负责的组件。

业务模块可以声明：

```xml
<activity android:name=".ui.OrderDetailActivity" />
```

不推荐在业务模块声明其他模块的组件。

壳工程负责最终合并 Manifest，但不应该随意接管业务模块内部声明。

---

## 17. 公共能力下沉规则

当代码被多个业务模块使用时，需要判断是否应该下沉。

### 17.1 可以下沉到 lib 的情况

满足以下条件时，可以放入 `libs`：

- 无业务含义
- 多个模块可复用
- 逻辑稳定
- 不依赖具体业务数据

例如：

```text
DateUtils
StringUtils
LoadingDialog
StateLayout
ImageLoader
LogUtils
```

### 17.2 可以下沉到 support 的情况

满足以下条件时，可以放入 `support`：

- 是业务运行基础能力
- 多个业务模块依赖
- 需要统一管理
- 可能依赖 SDK 或系统能力

例如：

```text
网络请求
路由跳转
本地存储
权限申请
埋点上报
定位能力
推送能力
```

### 17.3 不应该下沉的情况

以下内容不应下沉：

```text
UserManager
OrderManager
PayManager
CouponRepository
ProductDetailViewModel
```

这些都带有明显业务属性，应放在对应业务模块。

---

## 18. 常见错误示例

### 18.1 业务模块直接互相依赖

```kotlin
implementation(project(":business:module-user")) // ❌
```

建议改为：

```kotlin
implementation(project(":api:api-user")) // ✅
```

### 18.2 工具模块写业务逻辑

```text
lib-common/
└── LoginHelper.kt  ❌
```

建议改为：

```text
module-user/
└── LoginHelper.kt  ✅
```

### 18.3 app 壳工程越来越臃肿

错误做法：

```text
app/
├── LoginActivity.kt
├── OrderActivity.kt
├── PayActivity.kt
├── UserRepository.kt
└── OrderRepository.kt
```

推荐做法：

```text
module-user/
├── LoginActivity.kt
└── UserRepository.kt

module-order/
├── OrderActivity.kt
└── OrderRepository.kt

module-pay/
├── PayActivity.kt
└── PayRepository.kt
```

### 18.4 支撑模块包含业务判断

错误示例：

```kotlin
// support-network 中判断订单业务错误码
if (code == 30012) {
    showOrderExpiredDialog()
}
```

推荐：

```kotlin
// support-network 只抛出统一异常
throw ApiException(code, message)

// module-order 自己处理订单错误码
if (exception.code == ORDER_EXPIRED) {
    showOrderExpiredDialog()
}
```

---

## 19. Code Review 检查项

每次新增模块或调整依赖时，需要检查以下内容：

- 是否存在业务模块直接依赖业务模块
- 是否存在循环依赖
- 是否把业务逻辑写进了 app
- 是否把业务逻辑写进了 lib
- 是否把具体业务逻辑写进了 support
- 模块命名是否符合规范
- 包名是否符合规范
- 资源名是否有模块前缀
- 公共能力是否过早抽象
- 是否更新了本文档
- 是否影响模块独立运行
- 是否影响构建速度

---

## 20. 当前模块清单

### 20.1 壳工程

| 模块 | 说明 | 负责人 |
|---|---|---|
| app | 应用主入口、模块组装、全局初始化 | - |

### 20.2 业务模块

| 模块 | 说明 | 负责人 |
|---|---|---|
| module-home | 首页业务 | - |
| module-user | 用户、登录、账号相关业务 | - |
| module-order | 订单相关业务 | - |
| module-pay | 支付相关业务 | - |
| module-message | 消息通知相关业务 | - |

### 20.3 支撑模块

| 模块 | 说明 | 负责人 |
|---|---|---|
| support-router | 统一路由能力 | - |
| support-network | 网络请求能力 | - |
| support-storage | 本地存储能力 | - |
| support-database | 数据库能力 | - |
| support-analytics | 埋点能力 | - |
| support-permission | 权限申请能力 | - |

### 20.4 工具模块

| 模块 | 说明 | 负责人 |
|---|---|---|
| lib-common | 通用工具方法 | - |
| lib-ui | 通用 UI 组件 | - |
| lib-log | 日志工具 | - |
| lib-image | 图片加载封装 | - |
| lib-extension | Kotlin 扩展方法 | - |

### 20.5 API 模块

| 模块 | 说明 | 负责人 |
|---|---|---|
| api-user | 用户模块对外接口 | - |
| api-order | 订单模块对外接口 | - |
| api-pay | 支付模块对外接口 | - |

---

## 21. 维护要求

本文档需要随着项目结构变化同步维护。

以下情况必须更新本文档：

- 新增业务模块
- 删除业务模块
- 新增支撑模块
- 新增工具模块
- 调整模块依赖关系
- 修改模块命名
- 新增 API 模块
- 调整模块通信方式
- 修改模块分层规则

---

## 22. 总结

本项目模块划分的核心原则是：

```text
app 负责组装
business 负责业务
support 负责基础能力
libs 负责通用工具
api 负责模块解耦
```

依赖方向必须保持清晰：

```text
app → business → support → libs
business → api
business 之间不直接强依赖
```

所有新增模块都应该遵守本文档约定，避免项目随着业务增长变得难以维护。
