# /flow - 创建 Kotlin Flow 数据流

设计和实现 Kotlin Flow 数据流管道。

## 步骤

1. 确认数据流需求：数据源、转换逻辑、消费方式
2. 根据场景选择合适的 Flow 类型：
   - **Cold Flow**（`flow {}`）：一次性数据获取，每个收集者独立执行
   - **StateFlow**：UI 状态管理，有初始值，只保留最新值
   - **SharedFlow**：事件广播，可配置 replay 和缓冲策略
   - **Channel**：一对一通信，生产者-消费者模式
3. 生成代码：
   - 数据源层：`flow {}` / `callbackFlow {}` 包装回调 API
   - 转换层：`map`、`filter`、`combine`、`flatMapLatest` 等操作符
   - 消费层：`collectLatest`、`launchIn`、`stateIn`、`shareIn`
4. 在 ViewModel 中使用 `viewModelScope` 管理协程生命周期
5. 在 UI 层使用 `repeatOnLifecycle(STARTED)` 安全收集

## 常见模式

```kotlin
// 搜索防抖
searchQuery
    .debounce(300)
    .distinctUntilChanged()
    .flatMapLatest { query -> repository.search(query) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

// 多数据源合并
combine(userFlow, settingsFlow) { user, settings -> UiState(user, settings) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UiState())
```