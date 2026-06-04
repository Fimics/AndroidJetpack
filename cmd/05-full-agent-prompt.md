# 完整版 Agent 指令：Android 多模块项目文档与依赖分析

下面是一份完整的 Agent 指令，可以直接复制给 Agent 使用。  
如果项目比较大，建议先使用 `01-analyze-only.md`，不要一开始就使用完整版。

---

## 可复制指令

```md
你是一个资深 Android 架构工程师，请帮助我分析并完善当前 Android 多模块项目的架构文档。

## 项目背景

当前项目是一个 Android 多模块工程，可能包含以下模块：

- app 壳工程
- business/module-xxx 业务模块
- support/support-xxx 支撑模块
- libs/lib-xxx 工具模块
- api/api-xxx API 解耦模块

## 总体目标

请帮助我完成以下事情：

1. 分析当前项目模块结构
2. 判断各模块类型和职责
3. 检查模块依赖关系是否合理
4. 生成或完善多模块架构文档
5. 输出依赖风险和后续改进建议

## 执行原则

请严格遵守以下原则：

- 优先分析，不要贸然修改。
- 如果本轮需要修改文件，只允许修改 docs/ 目录。
- 不要修改业务代码。
- 不要删除任何已有文件。
- 不要移动源码文件。
- 不要修改 Gradle 配置，除非我后续明确授权。
- 不要重命名模块。
- 如果无法判断某个模块职责，请标记为“待补充”。
- 如果判断不确定，请明确标注“不确定，需要人工确认”。
- 不要编造项目中不存在的模块。
- 文档内容使用中文。

## 第一步：项目结构分析

请先扫描以下内容：

- settings.gradle / settings.gradle.kts
- 根 build.gradle / build.gradle.kts
- app 目录
- business 目录
- support 目录
- libs 目录
- api 目录
- 各模块 build.gradle / build.gradle.kts
- 已存在的 README.md 或 docs 文档

请输出：

- 当前模块清单
- 模块分类建议
- 壳工程说明
- 业务模块清单
- 支撑模块清单
- 工具模块清单
- API 模块清单
- 无法判断职责的模块
- 当前依赖关系总结
- 初步风险点

## 第二步：生成架构文档

如果 docs 目录不存在，请创建 docs 目录。

请生成或完善以下文件：

```text
docs/README-module-index.md
docs/module-architecture.md
docs/dependency-rules.md
docs/module-template.md
```

如果文件已存在，请基于原内容增量完善，不要直接覆盖。

### docs/module-architecture.md 内容要求

需要包含：

- 文档目的
- 项目整体结构
- 推荐目录结构
- 模块分层说明
- 壳工程职责
- 业务模块说明
- 支撑模块说明
- 工具模块说明
- API 模块说明
- 模块通信方式
- 模块依赖规范
- 模块命名规范
- 新增模块流程
- 模块独立运行说明
- 资源命名规范
- Manifest 规范
- 公共能力下沉规则
- 常见错误示例
- Code Review 检查项
- 当前模块清单
- 文档维护要求

### docs/dependency-rules.md 内容要求

需要包含：

- 模块分层规则
- 允许依赖关系
- 禁止依赖关系
- 依赖方向图
- business 模块之间通信方式
- app、business、support、libs、api 的 Gradle 依赖示例
- 常见违规场景
- Code Review 检查清单
- 依赖治理建议

### docs/module-template.md 内容要求

需要包含：

- 模块基本信息模板
- 新增业务模块模板
- 新增支撑模块模板
- 新增工具模块模板
- 新增 API 模块模板
- settings.gradle 示例
- build.gradle 示例
- README 模板
- Pull Request 检查清单
- 新增模块登记模板

### docs/README-module-index.md 内容要求

需要包含：

- 文档列表
- 每个文档的用途
- 推荐阅读顺序
- README.md 中如何引用这些文档

## 第三步：依赖风险分析

请检查当前模块依赖是否符合以下规则：

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

请输出：

- 合理依赖列表
- 可疑依赖列表
- 明确违规依赖列表
- 是否存在循环依赖风险
- 是否存在跨层依赖风险
- 是否需要新增 api 模块
- 是否需要拆分公共能力
- 后续治理建议

## 输出要求

完成后请输出：

1. 分析摘要
2. 新增文件列表
3. 修改文件列表
4. 发现的问题
5. 依赖风险报告
6. 需要人工确认的事项
7. 后续建议

## 验收标准

完成后应满足：

- docs 目录下存在完整的模块架构文档
- 新人可以通过文档理解项目模块划分
- 开发者可以根据文档判断新增模块应该放在哪里
- Code Review 时可以根据文档判断模块依赖是否合理
- 没有修改业务源码
- 没有修改 Gradle 配置
- 没有删除或移动已有文件
```

---

## 使用建议

完整版适合：

- 项目规模不大
- 你已经信任当前 Agent
- 你希望一次性得到分析和文档
- 你能接受它在 docs 目录内创建或修改文件

如果项目较大，建议先执行：

```text
01-analyze-only.md
```

确认结果准确后，再执行：

```text
02-generate-docs-only.md
```
