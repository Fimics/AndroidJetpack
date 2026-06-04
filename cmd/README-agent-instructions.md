# Android 多模块项目 Agent 指令文档索引

这组文档用于指导 AI Agent / 编程助手安全地分析、整理和完善 Android 多模块项目。

## 文档列表

| 文件 | 用途 |
|---|---|
| [01-analyze-only.md](01-analyze-only.md) | 只分析项目结构，不修改代码 |
| [02-generate-docs-only.md](02-generate-docs-only.md) | 只生成或完善 docs 文档 |
| [03-check-dependency-rules.md](03-check-dependency-rules.md) | 检查 Gradle 模块依赖风险 |
| [04-safe-workflow.md](04-safe-workflow.md) | 推荐的分阶段安全执行流程 |
| [05-full-agent-prompt.md](05-full-agent-prompt.md) | 完整版 Agent 指令，可直接复制使用 |

## 推荐使用方式

建议不要一开始就让 Agent 修改项目结构，而是按下面顺序执行：

```text
第一步：只分析项目结构
第二步：生成 docs 文档
第三步：检查 Gradle 依赖风险
第四步：人工确认后再考虑重构
```

最稳妥的第一条指令是：

```md
先不要改代码，请只分析当前 Android 项目的多模块结构，并给出模块分类、依赖风险和文档落地建议。
```

## 适用项目

适用于包含以下模块类型的 Android 项目：

- app 壳工程
- business/module-xxx 业务模块
- support/support-xxx 支撑模块
- libs/lib-xxx 工具模块
- api/api-xxx API 解耦模块
