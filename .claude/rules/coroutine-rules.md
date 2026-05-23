# 协程与异步编程规范

## 协程作用域选择
- **ViewModel**：`viewModelScope`（自动跟随 ViewModel 生命周期取消）
- **Activity/Fragment**：`lifecycleScope`（自动跟随生命周期取消）
- **Application 级**：自定义 `CoroutineScope(SupervisorJob() + Dispatchers.Default)`
- **Worker**：`CoroutineWorker` 自带协程支持
- 禁止使用 `GlobalScope`

## 调度器使用
- `Dispatchers.Main` — UI 操作（默认在 ViewModel/lifecycleScope 中）
- `Dispatchers.IO` — 网络请求、文件读写、数据库操作
- `Dispatchers.Default` — CPU 密集型计算（排序、解析、加密）
- 在 Repository 层用 `withContext(Dispatchers.IO)` 切换，不在 ViewModel 中切

## Flow 规范
- 数据库查询返回 `Flow<List<T>>`，实现实时更新
- 网络请求用 `suspend` 函数，不用 Flow 包装单次调用
- UI 收集 Flow 必须在 `repeatOnLifecycle(Lifecycle.State.STARTED)` 中
- 使用 `stateIn()` 将 Flow 转为 StateFlow 时指定 `WhileSubscribed(5000)`
- 搜索场景用 `debounce()` + `distinctUntilChanged()` + `flatMapLatest()`

## 异常处理
- 在 Repository 层 catch 异常并转为 `Result<T>`
- ViewModel 中用 `try-catch` 或 `runCatching {}` 处理
- 不要吞异常，至少用 KLog 记录
- 网络异常统一转换为用户可读的错误消息

## 取消处理
- 长时间操作要检查 `isActive` 或使用 `ensureActive()`
- `callbackFlow` 中必须在 `awaitClose {}` 清理资源
- `suspendCancellableCoroutine` 中注册 `invokeOnCancellation`

## 禁止项
- 禁止 `runBlocking` 在主线程使用
- 禁止在协程中用 `Thread.sleep()`，用 `delay()`
- 禁止手动创建线程执行异步任务（用协程替代）
- 禁止忽略 `Job` 返回值当需要取消时