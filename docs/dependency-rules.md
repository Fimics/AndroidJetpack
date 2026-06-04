# Android 多模块依赖规则

## 1. 文档目的

本文档用于专门说明 Android 多模块项目中的依赖约束，帮助团队在开发、重构和 Code Review 时判断依赖是否合理。

---

## 2. 模块分层

项目模块建议分为以下几层：

```text
app
 ↓
business
 ↓
support
 ↓
libs
```

另外可以增加 `api` 层，用于业务模块之间的接口解耦。

```text
business A  --->  api-B  <---  business B
```

---

## 3. 核心原则

模块依赖需要遵循以下原则：

- 上层可以依赖下层
- 下层不能反向依赖上层
- 同层模块尽量不互相依赖
- 业务模块之间不直接依赖
- 业务模块之间通过 `api` 或 `router` 通信
- 工具模块不能有业务含义
- 支撑模块不能包含具体业务判断
- 壳工程只做组装，不写复杂业务

---

## 4. 允许依赖关系

| 当前模块 | 可以依赖 |
|---|---|
| app | business、support、libs、api |
| business | support、libs、api |
| support | libs |
| libs | Android 基础库、Kotlin 标准库、稳定第三方库 |
| api | 基础类型、少量 libs，不依赖 business |

---

## 5. 禁止依赖关系

| 当前模块 | 禁止依赖 |
|---|---|
| libs | app、business、support |
| support | app、business |
| business | app |
| api | business |
| business A | business B，除非经过架构评审 |

---

## 6. 依赖方向图

推荐：

```text
app
 ├── business:module-home
 ├── business:module-user
 ├── business:module-order
 └── business:module-pay

business:module-order
 ├── api:api-user
 ├── support:support-router
 ├── support:support-network
 └── libs:lib-common

support:support-network
 ├── libs:lib-log
 └── libs:lib-common

libs:lib-common
 └── 第三方基础库
```

不推荐：

```text
libs:lib-ui              ---> business:module-user     ❌
support:support-network  ---> business:module-order    ❌
business:module-order    ---> app                      ❌
business:module-order    ---> business:module-user     ❌
api:api-user             ---> business:module-user     ❌
```

---

## 7. 业务模块之间如何通信

### 7.1 页面跳转

通过路由模块完成。

推荐：

```kotlin
Router.open("/user/profile")
    .withString("userId", userId)
    .navigation(context)
```

不推荐：

```kotlin
val intent = Intent(context, UserProfileActivity::class.java)
context.startActivity(intent)
```

### 7.2 能力调用

通过 API 模块暴露接口。

推荐依赖关系：

```text
module-order  --->  api-user  <---  module-user
```

示例：

```kotlin
interface UserService {
    fun isLogin(): Boolean
    fun getUserId(): String?
}
```

使用方：

```kotlin
class OrderRepository(
    private val userService: UserService
) {
    fun loadOrderList() {
        val userId = userService.getUserId()
    }
}
```

### 7.3 状态通知

低频全局事件可以通过事件通知。

适合：

- 登录状态变化
- 主题变化
- 语言变化
- 全局配置变化

不适合：

- 页面参数传递
- 高频数据同步
- 强业务流程控制

---

## 8. Gradle 依赖示例

### 8.1 app

```kotlin
dependencies {
    implementation(project(":business:module-home"))
    implementation(project(":business:module-user"))
    implementation(project(":business:module-order"))
    implementation(project(":business:module-pay"))

    implementation(project(":support:support-router"))
    implementation(project(":support:support-network"))
    implementation(project(":support:support-storage"))

    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-ui"))
}
```

### 8.2 business 模块

```kotlin
dependencies {
    implementation(project(":api:api-user"))
    implementation(project(":support:support-router"))
    implementation(project(":support:support-network"))
    implementation(project(":support:support-storage"))
    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-ui"))
}
```

### 8.3 support 模块

```kotlin
dependencies {
    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-log"))
}
```

### 8.4 libs 模块

```kotlin
dependencies {
    implementation("androidx.core:core-ktx:x.x.x")
}
```

---

## 9. 常见违规场景

### 9.1 business 直接依赖 business

```kotlin
implementation(project(":business:module-user")) // ❌
```

应改为：

```kotlin
implementation(project(":api:api-user")) // ✅
```

### 9.2 support 依赖具体业务

```kotlin
implementation(project(":business:module-order")) // ❌
```

应将业务逻辑放回业务模块。

### 9.3 lib 中出现业务类

```text
lib-common/
└── UserSession.kt     ❌
```

应放到：

```text
module-user/
└── UserSession.kt     ✅
```

### 9.4 app 中堆积业务逻辑

```text
app/
├── LoginRepository.kt
├── OrderRepository.kt
└── PayManager.kt
```

应分别移动到对应业务模块。

---

## 10. Code Review 检查清单

提交代码时重点检查：

- 是否新增了不合理的模块依赖
- 是否存在循环依赖
- 是否有 business 直接依赖 business
- 是否有 support 依赖 business
- 是否有 libs 依赖 support 或 business
- 是否将业务逻辑写入了 app
- 是否将业务逻辑写入了 lib
- 是否将业务判断写入了 support
- 是否新增公共能力但没有判断下沉位置
- 是否新增模块后同步更新文档

---

## 11. 依赖治理建议

建议定期检查依赖关系：

- 每次新增模块时检查一次
- 每次发版前检查一次
- 每次较大重构后检查一次
- Code Review 中重点关注 `build.gradle` 变化
- 对公共模块新增依赖保持谨慎
- 禁止为了解决临时编译问题随意添加跨层依赖

---

## 12. 总结

依赖规则的核心是：

```text
app 组装业务
business 承载业务
support 提供基础能力
libs 提供通用工具
api 负责模块解耦
```

保持依赖方向清晰，是多模块项目长期可维护的关键。
