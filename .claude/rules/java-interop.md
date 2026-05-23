# Java 代码规范与 Kotlin 互操作

## 项目中的 Java 代码
本项目主要使用 Kotlin，但 libcore 和部分模块保留了 Java 代码。编写或修改 Java 代码时遵循以下规范。

## Java 编码规范
- 类成员顺序：常量 → 静态变量 → 实例变量 → 构造器 → 公有方法 → 私有方法
- 工具类使用 `private` 构造器防止实例化
- 方法参数加 `@NonNull` / `@Nullable` 注解（对 Kotlin 调用者至关重要）
- 资源使用 try-with-resources：`try (InputStream is = ...) {}`
- 集合声明使用接口类型：`List<T>` 而非 `ArrayList<T>`

## Kotlin 调用 Java 注意事项
- Java 方法未标注 `@Nullable/@NonNull` 时返回平台类型（`T!`），需显式声明可空性
- Java 的 `static` 方法在 Kotlin 中直接通过类名调用
- Java SAM 接口在 Kotlin 中用 lambda 替代
- Java getter/setter 在 Kotlin 中作为属性访问

## Java 调用 Kotlin 注意事项
- Kotlin 顶层函数编译为 `FileNameKt.method()`
- `companion object` 中的方法需要 `@JvmStatic` 才能作为静态方法调用
- Kotlin 默认参数需要 `@JvmOverloads` 生成重载方法
- Kotlin `data class` 的 `copy()` / `componentN()` 在 Java 中可用但不常用

## 新代码语言选择
- 新文件优先使用 Kotlin
- 修改现有 Java 文件时保持 Java（除非整体迁移）
- 新的 Activity/Fragment/ViewModel 必须用 Kotlin
- 工具类可保持 Java（如 libcore 中已有的）