# Jetpack Navigation vs ARouter 选型对比

> 适用工程：**AiGuide**（多模块，单 Activity + 计划 Compose/Flow）
>
> 结论先行：本工程**采用 Jetpack Navigation + `api-*` 能力接口 + Hilt** 的组合，本质上用官方组件拼出 ARouter 的整套能力，且类型安全、与 Lifecycle/Compose 集成更好、官方长期维护。新项目不必再引 ARouter。

---

## 1. 定位：两者根本不是一类东西

- **ARouter**：阿里开源的**路由总线**。一个框架同时管「页面路由 + 服务发现 + 拦截器 + 参数注入」，靠 `path` 字符串驱动，基于 APT 在编译期生成路由表（`path → 目标类`）。
- **Jetpack Navigation**：Google 官方的**应用内导航框架**。核心是 `NavController` + `NavGraph`，专注「返回栈管理 + 转场动画 + 深链 + BottomNav/Drawer 集成」，围绕单 Activity + 多 Fragment/Composable。

> 一句话：**ARouter 是"全家桶路由总线"，Navigation 是"专注导航的官方组件"**。ARouter 多出来的是「服务发现 + 拦截器」，少的是「返回栈/转场/Compose 集成/类型安全」。

---

## 2. 全维度对比

| 维度 | **ARouter** | **Jetpack Navigation** |
| --- | --- | --- |
| 本质定位 | 路由总线：页面路由 + 服务发现 + 拦截器 | 导航框架：返回栈 + 转场 + 深链 |
| 跳转标识 | `path` 字符串 `/module/page` | destination `@id` / route 字符串 / URI |
| 路由表来源 | APT 编译期生成（`path → 目标类`） | NavGraph（XML 或 Kotlin DSL） |
| 跨模块解耦 | **强**：靠 path，A 跳 B **完全不依赖 B** | 需配合手段（route / deepLink + 子图 include） |
| 类型安全 | 弱（字符串 + `@Autowired` 反射注参） | **强**（Safe Args；2.8+ 有 typed route） |
| 服务发现（跨模块调能力） | **内置** `IProvider` | 无（导航框架不负责，配合 Hilt/SPI） |
| 拦截器（登录/降级） | **内置** `IInterceptor` 拦截链 | 无（需自封装 listener/门面） |
| 返回栈 / 转场 / 深链 | 一般（偏 Activity 体系） | **强项**，官方支持完善 |
| 与 Lifecycle/ViewModel | 弱 | **深度集成**（navGraphViewModels 等） |
| 与 Compose | 弱 | **官方 navigation-compose** |
| 编译期处理 | APT / kapt（构建慢） | Safe Args Gradle 插件 |
| 维护状态 | 社区维护，更新趋缓 | Google 官方，活跃迭代 |
| 主要跳转目标 | Activity（也可取 Fragment 实例） | Fragment / Composable（单 Activity） |

---

## 3. Jetpack Navigation 能否实现 ARouter 类似功能？

**能，但需要"拼"** —— 把 ARouter 的四大能力逐一对应到官方组件：

### 3.1 跨模块页面跳转（不依赖目标类）—— ✅ 能

两种做法，第二种最接近 ARouter 的 `path`：

**做法 A：子图 + route**（本工程 `support-router` 主路线，见 `01-arch.md` §5.3/§5.6）

各业务模块自带 `xxx_nav_graph`，`app` 层 `include` 汇总；跳转用 route，不 import 目标 Fragment 类：

```kotlin
navController.navigate(Routes.chatDetail(conversationId))
```

**做法 B：隐式 Deep Link（最像 ARouter path）**

目标页在**自己模块**的子图里声明一个 URI：

```xml
<!-- business/module-chat/.../chat_nav_graph.xml -->
<fragment android:name="com.mic.guide.module.chat.ui.ChatDetailFragment">
    <deepLink app:uri="aiguide://chat/detail/{conversationId}" />
</fragment>
```

其他模块只靠字符串跳转，**零依赖目标类**，效果等同 `ARouter.build("/chat/detail").navigation()`：

```kotlin
navController.navigate("aiguide://chat/detail/123".toUri())
```

### 3.2 服务发现 / 跨模块调用能力（ARouter `IProvider`）—— ✅ 用 api + Hilt

Navigation **不负责**这件事。本工程用 **`api-*` 能力接口 + Hilt** 解决（见 `01-arch.md` §6），等价于 ARouter 的 Provider，而且**类型安全**：

```kotlin
// api/api-music/MusicApi.kt —— 只放接口
interface MusicApi { fun play(songId: String) }

// business/module-music —— 实现并 @Binds 到 Hilt
// business/module-home —— 仅依赖 api-music，注入接口调用
@Inject lateinit var musicApi: MusicApi
musicApi.play("1001")
```

### 3.3 拦截器（登录校验 / 降级）—— ✅ 自封一层

ARouter 内置拦截链；Navigation 需自己封装。两种常见方式：

```kotlin
// 方式一：全局监听目标变化做前置校验
navController.addOnDestinationChangedListener { _, dest, _ ->
    if (dest.id in needLoginDestinations && !userManager.isLogin) {
        navController.navigate(Routes.LOGIN)
    }
}

// 方式二：在 support-router 的 AppNavigator.navigate() 门面里统一前置校验/降级
```

### 3.4 参数注入（ARouter `@Autowired`）—— ✅ Safe Args 更好

ARouter 用反射注参，运行期才暴露错误；Navigation 的 **Safe Args** 编译期生成类型安全的 `Directions`/`Args`：

```kotlin
val action = ChatListFragmentDirections.actionListToDetail(conversationId)
findNavController().navigate(action)
```

### 能力映射小结

| ARouter 能力 | Jetpack Navigation 侧实现 |
| --- | --- |
| `path` 页面跳转 | route 字符串 / 隐式 deepLink URI |
| `IProvider` 服务发现 | `api-*` 接口 + Hilt（`@Binds`/`@EntryPoint`） |
| `IInterceptor` 拦截链 | `addOnDestinationChangedListener` 或 `AppNavigator` 门面 |
| `@Autowired` 注参 | Safe Args（类型安全） |
| URL Scheme / 外部唤起 | `<deepLink>` 显式深链 |

---

## 4. 版本注意点（本工程）

- 当前 `gradle/libs.versions.toml` 里 **Navigation = 2.5.1**。
- **typed-route**（基于 `kotlinx-serialization` 的类型安全路由）需 **Navigation 2.8+**。
- 因此在 2.5.1 上，跨模块 path 化只能走 **route 字符串 / 隐式 deepLink**；Safe Args（XML directions）可用。
- 若想要更强的类型安全跳转（Kotlin 对象即路由），可评估升级到 **2.8.x**。

---

## 5. 选型结论与建议

**本工程采用 Jetpack Navigation 组合方案**：

```
Jetpack Navigation   →  页面导航 / 返回栈 / 转场 / 深链
        +
api-* 能力接口        →  服务发现（替代 IProvider）
        +
Hilt                 →  依赖注入 / 接口实现绑定
        +
AppNavigator 门面     →  统一跳转入口 / 拦截 / 降级（替代 IInterceptor）
```

理由：
1. **官方维护、活跃迭代**，与 Lifecycle / ViewModel / Compose 深度集成。
2. **类型安全**优于 ARouter 的字符串 + 反射。
3. ARouter 多出的「服务发现 + 拦截器」已被 `api-*` + Hilt + `AppNavigator` 覆盖。
4. 单 Activity + 计划 Compose/Flow 的技术走向，Navigation 是顺势而为。

**什么时候才真考虑 ARouter**：
- 纯多 Activity 老架构、改造成本高；
- 已有大量 H5 ↔ 原生统一 URL 调度体系；
- 需要开箱即用的拦截 / 降级 / 分组按需加载，且不想自己封装。

> 对 AiGuide 这种新建多模块工程，**Navigation 组合方案更优**，无需引入 ARouter。

---

## 6. 一句话速记

> **页面跳转用 Navigation，跨模块能力用 `api-*` + Hilt，拦截用 `AppNavigator` 门面**——这套官方组合既解耦又类型安全，等价并优于 ARouter。

参见：`docs/01-arch.md` §5（路由与 Navigation）、§6（api 能力接口层）、§11（依赖规则与模块通信）。
