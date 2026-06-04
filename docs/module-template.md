# Android 新增模块模板

## 1. 文档目的

本文档用于在新增 Android 模块时作为模板使用，帮助开发者明确模块职责、目录结构、依赖规则和 Code Review 检查项。

---

## 2. 模块基本信息

| 项目 | 内容 |
|---|---|
| 模块名称 | module-xxx / support-xxx / lib-xxx / api-xxx |
| 模块类型 | 业务模块 / 支撑模块 / 工具模块 / API 模块 |
| 模块负责人 | - |
| 创建时间 | - |
| 业务说明 | - |
| 是否支持独立运行 | 是 / 否 |
| 是否需要路由注册 | 是 / 否 |
| 是否需要对外暴露 API | 是 / 否 |

---

## 3. 模块类型判断

新增模块前，需要先判断模块属于哪一类。

| 类型 | 判断标准 | 示例 |
|---|---|---|
| 业务模块 | 承载具体业务流程 | module-user、module-order |
| 支撑模块 | 提供基础能力，不包含具体业务 | support-network、support-router |
| 工具模块 | 通用工具，无业务含义 | lib-common、lib-ui |
| API 模块 | 暴露业务能力接口 | api-user、api-pay |

---

## 4. 业务模块模板

### 4.1 目录位置

```text
business/module-xxx/
```

### 4.2 settings.gradle

```kotlin
include(":business:module-xxx")
```

### 4.3 build.gradle 示例

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.business.xxx"

    defaultConfig {
        minSdk = 23
    }
}

dependencies {
    implementation(project(":support:support-router"))
    implementation(project(":support:support-network"))
    implementation(project(":support:support-storage"))

    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-ui"))
    implementation(project(":libs:lib-log"))
}
```

### 4.4 推荐目录结构

```text
module-xxx/
├── src/main/
│   ├── java/com/example/business/xxx/
│   │   ├── ui/
│   │   ├── viewmodel/
│   │   ├── repository/
│   │   ├── datasource/
│   │   ├── model/
│   │   ├── router/
│   │   └── di/
│   ├── res/
│   └── AndroidManifest.xml
└── build.gradle
```

### 4.5 业务模块检查项

- 是否只包含当前业务域的逻辑
- 是否没有直接依赖其他 business 模块
- 是否通过 api 或 router 与其他模块通信
- 是否没有把公共工具类堆到当前模块
- 是否资源名称添加了模块前缀
- 是否需要新增对应 api 模块
- 是否更新了模块架构文档

---

## 5. 支撑模块模板

### 5.1 目录位置

```text
support/support-xxx/
```

### 5.2 settings.gradle

```kotlin
include(":support:support-xxx")
```

### 5.3 build.gradle 示例

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.support.xxx"
}

dependencies {
    implementation(project(":libs:lib-common"))
    implementation(project(":libs:lib-log"))
}
```

### 5.4 推荐目录结构

```text
support-xxx/
├── src/main/
│   ├── java/com/example/support/xxx/
│   │   ├── core/
│   │   ├── config/
│   │   ├── manager/
│   │   └── internal/
│   ├── res/
│   └── AndroidManifest.xml
└── build.gradle
```

### 5.5 支撑模块检查项

- 是否只提供基础能力
- 是否不包含具体业务逻辑
- 是否没有依赖 business 模块
- 是否没有依赖 app 模块
- 是否可以被多个业务模块复用
- 是否对外提供稳定接口
- 是否隐藏了第三方 SDK 细节

---

## 6. 工具模块模板

### 6.1 目录位置

```text
libs/lib-xxx/
```

### 6.2 settings.gradle

```kotlin
include(":libs:lib-xxx")
```

### 6.3 build.gradle 示例

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.libs.xxx"
}

dependencies {
    implementation("androidx.core:core-ktx:x.x.x")
}
```

### 6.4 推荐目录结构

```text
lib-xxx/
├── src/main/
│   ├── java/com/example/libs/xxx/
│   │   ├── util/
│   │   ├── ext/
│   │   └── widget/
│   ├── res/
│   └── AndroidManifest.xml
└── build.gradle
```

### 6.5 工具模块检查项

- 是否无业务含义
- 是否不依赖 business
- 是否不依赖 support，除非有明确理由
- 是否命名足够通用
- 是否可被多个模块复用
- 是否避免过早抽象

---

## 7. API 模块模板

### 7.1 目录位置

```text
api/api-xxx/
```

### 7.2 settings.gradle

```kotlin
include(":api:api-xxx")
```

### 7.3 build.gradle 示例

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.api.xxx"
}

dependencies {
    implementation(project(":libs:lib-common"))
}
```

### 7.4 推荐目录结构

```text
api-xxx/
├── src/main/
│   ├── java/com/example/api/xxx/
│   │   ├── XxxService.kt
│   │   ├── XxxInfo.kt
│   │   └── XxxProvider.kt
│   └── AndroidManifest.xml
└── build.gradle
```

### 7.5 API 模块检查项

- 是否只包含接口、数据结构、常量
- 是否不包含具体业务实现
- 是否不依赖 business 模块
- 是否不依赖 app 模块
- 是否接口足够稳定
- 是否避免暴露过多内部细节

---

## 8. 新增模块登记模板

将以下内容复制到 `module-architecture.md` 的模块清单中。

### 8.1 业务模块登记

```md
| module-xxx | xxx 业务 | 负责人 |
```

### 8.2 支撑模块登记

```md
| support-xxx | xxx 基础能力 | 负责人 |
```

### 8.3 工具模块登记

```md
| lib-xxx | xxx 通用工具 | 负责人 |
```

### 8.4 API 模块登记

```md
| api-xxx | xxx 模块对外接口 | 负责人 |
```

---

## 9. 新增模块 Pull Request 检查清单

提交 PR 前请确认：

- [ ] 已在 `settings.gradle` 中 include 新模块
- [ ] 模块命名符合规范
- [ ] 包名符合规范
- [ ] namespace 符合规范
- [ ] 依赖方向符合规范
- [ ] 没有产生循环依赖
- [ ] 没有业务模块直接依赖业务模块
- [ ] 没有支撑模块依赖业务模块
- [ ] 没有工具模块依赖业务模块
- [ ] 资源名称有模块前缀
- [ ] Manifest 只声明当前模块组件
- [ ] 新增公共能力的位置合理
- [ ] 新增路由已登记
- [ ] 新增 API 已登记
- [ ] 已更新 `module-architecture.md`
- [ ] 已更新相关 README 或使用说明

---

## 10. 模块说明模板

可以在每个模块根目录下新增 `README.md`。

```md
# module-xxx

## 模块职责

说明该模块负责哪些功能。

## 模块边界

说明该模块不负责哪些功能。

## 主要页面

| 页面 | 说明 |
|---|---|
| XxxActivity | xxx 页面 |
| XxxFragment | xxx 页面 |

## 对外路由

| 路由 | 参数 | 说明 |
|---|---|---|
| /xxx/detail | id | 打开详情页 |

## 对外 API

| API | 说明 |
|---|---|
| XxxService | xxx 能力 |

## 依赖模块

| 模块 | 说明 |
|---|---|
| support-router | 页面路由 |
| support-network | 网络请求 |
| lib-common | 通用工具 |

## 注意事项

- xxx
- xxx
```

---

## 11. 总结

新增模块时不要只关注“能不能编译通过”，更要关注：

```text
职责是否清晰
边界是否明确
依赖是否合理
后续是否可维护
```

每个新模块都应该能够回答以下问题：

- 它为什么存在？
- 它属于哪一层？
- 它能依赖谁？
- 谁可以依赖它？
- 它是否包含了不属于自己的逻辑？
