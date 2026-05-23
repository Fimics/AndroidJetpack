# /deps - 添加或查询依赖

管理项目依赖：查询当前依赖版本、添加新依赖、升级现有依赖。

## 步骤

1. 读取 `gradle/libs.versions.toml` 了解当前依赖配置
2. 根据用户需求执行操作：
   - **查询**：在 `libs.versions.toml` 中搜索并展示版本信息
   - **添加**：在 `[versions]`、`[libraries]`（可选 `[plugins]`）中添加条目
   - **升级**：修改 `[versions]` 中的版本号
3. 如果添加了新依赖，提示用户在对应模块的 `build.gradle.kts` 中引用
4. 运行 `./gradlew --refresh-dependencies` 验证依赖可解析

## 注意事项

- 所有版本统一在 `libs.versions.toml` 中管理
- 公共依赖优先加到 `libcore` 中用 `api()` 传递
- 模块私有依赖用 `implementation()` 声明