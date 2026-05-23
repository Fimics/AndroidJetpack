# /analyze - 分析模块代码

深度分析指定模块的代码结构、依赖关系、潜在问题。

## 步骤

1. 确认要分析的模块
2. 读取模块的 `build.gradle.kts` 了解依赖和配置
3. 扫描模块源码目录结构
4. 分析关键类和文件：
   - Activity / Fragment — UI 入口
   - ViewModel — 状态管理
   - Repository / Manager — 业务逻辑
   - Data classes / Models — 数据模型
5. 输出分析报告：
   - 模块职责概述
   - 核心类及其关系
   - 使用的架构模式（MVVM / MVP 等）
   - 依赖关系图
   - 发现的问题或改进建议