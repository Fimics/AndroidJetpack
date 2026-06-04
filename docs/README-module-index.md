# Android 多模块项目文档索引

本文档用于说明当前项目中与模块架构相关的文档位置。

## 文档列表

| 文档 | 说明 |
|---|---|
| [module-architecture.md](module-architecture.md) | Android 多模块架构主说明文档 |
| [dependency-rules.md](dependency-rules.md) | 模块依赖规则说明 |
| [module-template.md](module-template.md) | 新增模块模板与检查清单 |

## 推荐放置位置

建议将这些文档放在项目根目录的 `docs/` 目录下：

```text
project-root/
├── app/
├── business/
├── support/
├── libs/
├── api/
├── docs/
│   ├── README-module-index.md
│   ├── module-architecture.md
│   ├── dependency-rules.md
│   └── module-template.md
├── settings.gradle
├── build.gradle
└── README.md
```

## README.md 中的引用示例

可以在项目根目录的 `README.md` 中增加以下内容：

```md
## 模块架构

本项目采用 Android 多模块架构，详细说明见：

- [模块架构说明](docs/module-architecture.md)
- [模块依赖规则](docs/dependency-rules.md)
- [新增模块模板](docs/module-template.md)
```
