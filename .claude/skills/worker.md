# /worker - 创建 WorkManager 后台任务

快速生成 WorkManager Worker 及其调度代码。

## 步骤

1. 确认任务信息：任务名称、功能描述、触发方式（一次性 / 周期性）、约束条件
2. 生成以下代码：
   - **CoroutineWorker**：继承 `CoroutineWorker`，实现 `doWork()` 逻辑
   - **调度代码**：在合适位置创建 `WorkRequest` 并 enqueue
   - **输入/输出数据**：使用 `workDataOf()` 传递参数
3. 配置约束条件：
   - 网络要求（`NetworkType.CONNECTED` / `UNMETERED`）
   - 电量要求（`requiresBatteryNotLow`）
   - 存储要求（`requiresStorageNotLow`）
4. 周期性任务设置最小间隔（15 分钟）
5. 如果使用 Hilt，添加 `@HiltWorker` + `@AssistedInject`

## 常见场景

- 数据同步（周期性，需要网络）
- 日志上传（一次性，WiFi 下执行）
- 图片压缩（一次性，链式任务）
- 缓存清理（周期性，设备空闲时）