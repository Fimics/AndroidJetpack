# Agent 指令：检查 Android 多模块依赖风险

下面这段指令用于让 Agent 检查 Gradle 模块依赖是否合理。  
本指令只分析，不修改代码。

---

## 可复制指令

```md
请检查当前 Android 多模块项目的 Gradle 依赖关系，并输出依赖风险报告。

## 检查范围

请重点检查：

- settings.gradle / settings.gradle.kts
- 根 build.gradle / build.gradle.kts
- 各模块 build.gradle / build.gradle.kts
- app 模块依赖
- business 模块依赖
- support 模块依赖
- libs 模块依赖
- api 模块依赖

## 检查规则

请按照以下规则判断依赖是否合理：

1. app 可以依赖 business、support、libs、api
2. business 可以依赖 support、libs、api
3. support 可以依赖 libs
4. libs 不应该依赖 business、support、app
5. support 不应该依赖 business、app
6. business 不应该依赖 app
7. business 模块之间不建议直接依赖
8. api 模块不应该依赖 business
9. api 模块应尽量只包含接口、数据结构、常量
10. app 壳工程不应该承载复杂业务逻辑

## 输出内容

请输出：

- 当前模块依赖关系表
- 合理依赖列表
- 可疑依赖列表
- 明确违规依赖列表
- 是否存在循环依赖风险
- 是否存在跨层依赖风险
- 是否存在 business 直接依赖 business
- 是否存在 support 依赖 business
- 是否存在 libs 依赖业务模块
- 建议调整方案
- 是否需要新增 api 模块
- 是否需要拆分公共能力

## 风险等级

请将问题按以下级别分类：

| 等级 | 含义 |
|---|---|
| 高风险 | 已经违反依赖规则，可能导致循环依赖、强耦合或难以维护 |
| 中风险 | 当前可运行，但长期会影响维护和扩展 |
| 低风险 | 不影响运行，但建议规范化 |
| 待确认 | 信息不足，需要人工确认 |

## 约束条件

- 本轮只分析，不修改代码。
- 不新增文件。
- 不删除文件。
- 不修改 Gradle 配置。
- 不移动源码。
- 不要为了修复问题直接改项目。
- 如果依赖关系无法判断，请明确写“待确认”。

## 输出格式

请使用 Markdown 输出，并尽量使用表格展示结果。
```

---

## 使用建议

这个指令适合用于：

- 生成文档之后进一步检查实际依赖
- 重构前做风险评估
- Code Review 前自动检查模块依赖
- 准备引入 api 模块前分析调用关系
