# Android 架构规范

## MVVM 架构分层
```
View (Activity/Fragment/Composable)
  ↓ 观察 StateFlow/LiveData
ViewModel
  ↓ 调用
Repository
  ↓ 访问
DataSource (Room / Retrofit / DataStore)
```

## View 层规则
- Activity/Fragment 只负责：UI 渲染、用户事件转发、生命周期管理
- 不在 View 层做业务逻辑或数据转换
- XML 布局使用 ViewBinding（优先）或 DataBinding
- Compose 使用状态提升模式：Screen 接收 state + event lambda

## ViewModel 规则
- 使用 `StateFlow` 管理 UI 状态（本项目标准）
- UI 状态用 `data class XxxUiState` 封装，支持 `copy()` 更新
- 用 `viewModelScope` 启动协程，不手动创建 CoroutineScope
- 不持有 Activity/Fragment/View 引用（防止内存泄漏）
- 不引用 `android.R` 或资源 ID，用抽象事件传递

## Repository 规则
- 作为数据层的唯一入口，协调多个 DataSource
- 返回 `Flow<T>` 或 `suspend` 函数
- 处理缓存策略（网络优先 / 缓存优先）
- 异常转换为业务层可理解的 Result 类型

## 依赖注入
- Hilt 模块：`@HiltViewModel` + `@AndroidEntryPoint`
- Dagger 模块：手动 Component + Module 配置
- 无 DI 模块：通过构造函数注入，或 ViewModel Factory

## 状态管理模式
```kotlin
// 标准 UiState 模式
data class XxxUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)

// ViewModel 中
private val _uiState = MutableStateFlow(XxxUiState())
val uiState: StateFlow<XxxUiState> = _uiState.asStateFlow()

// 更新状态
_uiState.update { it.copy(isLoading = true) }
```