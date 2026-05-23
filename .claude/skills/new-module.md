# /new-module - 创建新模块

根据用户需求创建一个新的 Android 模块，遵循项目现有规范。

## 步骤

1. 询问用户：模块名称、模块类型（application / library）、父目录（可选）
2. 创建模块目录结构：
   ```
   <模块名>/
   ├── build.gradle.kts
   └── src/main/
       ├── AndroidManifest.xml
       ├── java/com/mic/<模块包名>/
       └── res/
           ├── layout/
           └── values/strings.xml
   ```
3. 编写 `build.gradle.kts`：
   - 使用 `libs.versions.*` 引用版本，不硬编码
   - 设置 `namespace`（com.mic.<模块名>）
   - compileSdk = 35, minSdk = 24, targetSdk = 35
   - 添加 `implementation(project(":libcore"))` 依赖
4. 在 `settings.gradle.kts` 中添加 `include(":<模块路径>")`
5. 验证：运行 `./gradlew :<模块路径>:assembleDebug` 确认可编译