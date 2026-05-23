# Kotlin 编码规范

## 命名规范
- 类名：大驼峰 `UserRepository`、`BleClientViewModel`
- 函数/变量：小驼峰 `getUserById`、`isConnected`
- 常量：全大写下划线 `MAX_RETRY_COUNT`、`DEFAULT_TIMEOUT`
- 包名：全小写 `com.mic.ble.client`
- 文件名：与主要类名一致，扩展函数文件用 `XxxExt.kt`

## 语言特性优先级
- 优先用 `data class` 表示纯数据结构
- 优先用 `sealed class/interface` 表示有限状态集合（UI State、Result）
- 优先用 `object` 实现单例，避免手写双重检查锁
- 优先用扩展函数替代工具类静态方法
- 优先用 `when` 替代多层 `if-else`
- 优先用 `?.let {}` / `?.run {}` 处理可空值，避免 `!!`
- 集合操作优先用 `map`/`filter`/`flatMap`，避免手写 for 循环

## 作用域函数选择
- `let` — 非空执行 + 变换：`user?.let { save(it) }`
- `apply` — 对象初始化配置：`Intent().apply { action = "xxx" }`
- `run` — 执行代码块并返回结果
- `also` — 附加副作用（日志、调试）：`data.also { log(it) }`
- `with` — 对同一对象多次操作

## 字符串
- 优先用字符串模板 `"User: $name"` 而非拼接
- 多行字符串用 `trimIndent()`

## 禁止项
- 禁止使用 `!!` 除非有充分注释说明为何安全
- 禁止在 Kotlin 中使用 Java Stream API，用 Kotlin 集合函数
- 禁止 `var` 能用 `val` 的地方
- 禁止裸 `catch (e: Exception)`，至少记录日志