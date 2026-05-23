# /mvvm - 创建 MVVM 页面

快速搭建一个完整的 MVVM 架构页面（Activity/Fragment + ViewModel + Repository）。

## 步骤

1. 确认页面信息：页面名称、功能描述、所属模块、UI 方式（Compose / XML）
2. 生成以下文件：

### XML 方式
- **Activity/Fragment**：ViewBinding/DataBinding 绑定，观察 ViewModel 状态
- **ViewModel**：StateFlow/LiveData 管理 UI 状态，调用 Repository
- **Repository**（可选）：数据获取逻辑（网络 + 本地缓存）
- **布局 XML**：activity_xxx.xml / fragment_xxx.xml
- 在 `AndroidManifest.xml` 中注册 Activity

### Compose 方式
- **Screen Composable**：UI 层，接收 state 和 event
- **ViewModel**：UiState data class + StateFlow
- **Repository**（可选）

3. 如果模块使用 Hilt，添加 `@HiltViewModel` 和 `@AndroidEntryPoint` 注解
4. 验证编译通过

## UI State 模式

```kotlin
data class XxxUiState(
    val isLoading: Boolean = false,
    val data: List<Item> = emptyList(),
    val error: String? = null
)
```