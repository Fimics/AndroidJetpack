# Agent 指令：只生成 Android 多模块架构文档

下面这段指令适合在 Agent 已经分析完项目结构后使用。  
它只允许 Agent 修改 `docs/` 目录，风险较低。

---

## 可复制指令

```md
请根据当前 Android 项目的实际结构，生成或完善 Android 多模块架构文档。

## 本次允许修改的范围

只允许新增或修改以下目录：

```text
docs/
```

只允许新增或修改以下文件：

```text
docs/README-module-index.md
docs/module-architecture.md
docs/dependency-rules.md
docs/module-template.md
```

## 本次禁止操作

- 禁止修改 app、business、support、libs、api 下的源码
- 禁止修改 settings.gradle / settings.gradle.kts
- 禁止修改 build.gradle / build.gradle.kts
- 禁止移动文件
- 禁止删除文件
- 禁止重命名模块
- 禁止调整 Gradle 依赖
- 禁止格式化整个项目

## 文档内容要求

请结合当前项目真实模块，完成以下文档。

### 1. docs/module-architecture.md

需要包含：

- 项目整体结构说明
- 壳工程职责说明
- 业务模块清单
- 支撑模块清单
- 工具模块清单
- API 模块清单
- 模块分层说明
- 推荐依赖方向
- 禁止依赖方向
- 模块通信方式
- 新增模块流程
- 资源命名规范
- Manifest 规范
- Code Review 检查项
- 当前模块清单和负责人字段

### 2. docs/dependency-rules.md

需要包含：

- 模块分层规则
- 允许依赖关系
- 禁止依赖关系
- business 模块之间通信方式
- app、business、support、libs、api 的依赖示例
- 常见错误示例
- Code Review 检查清单

### 3. docs/module-template.md

需要包含：

- 新增业务模块模板
- 新增支撑模块模板
- 新增工具模块模板
- 新增 API 模块模板
- settings.gradle 示例
- build.gradle 示例
- README 模板
- PR 检查清单

### 4. docs/README-module-index.md

需要作为 docs 目录下的文档索引，说明每个文档的用途。

## 细节要求

- 文档内容使用中文。
- 文档结构要清晰，适合团队长期维护。
- 文档中的模块名称要尽量根据当前项目真实目录生成。
- 如果目标 md 文件已存在，请基于原内容增量完善，不要直接覆盖。
- 如果当前项目中没有某类模块，例如 api 模块，请在文档中说明：

```text
当前项目暂未发现 api 模块，后续当业务模块之间需要解耦通信时建议引入。
```

- 如果无法判断某个模块职责，请标记为“待补充”，不要编造。
- 所有 Gradle 示例请优先使用当前项目已有 Gradle 风格。
- 如果项目使用 Kotlin DSL，请使用 build.gradle.kts 示例。
- 如果项目使用 Groovy DSL，请使用 build.gradle 示例。

## 完成后输出

请输出：

- 新增文件列表
- 修改文件列表
- 每个文件的主要内容摘要
- 未能确认的信息
- 后续建议
```

---

## 使用建议

这个指令适合用于：

- 已经确认项目模块分类后
- 只想补齐文档，不想动业务代码
- 希望给团队留下一份长期维护的架构说明
- Code Review 中需要明确模块依赖边界
