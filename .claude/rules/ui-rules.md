# UI 开发规范

## 布局选择
- 本项目同时使用 Compose 和传统 View 体系
- 新页面优先考虑 Compose（app-compose 模块）
- 传统 View 使用 ViewBinding（优先）或 DataBinding
- 禁止使用 `findViewById`

## XML 布局规范
- 优先用 `ConstraintLayout` 减少嵌套层级（目标 ≤ 3 层）
- 避免 `RelativeLayout` 嵌套 `RelativeLayout`
- `RecyclerView` 搭配 `ListAdapter` + `DiffUtil`（非 `notifyDataSetChanged`）
- 列表项布局用 `ViewBinding`，不在 Adapter 中 `inflate` 后 `findViewById`
- 字符串资源统一放 `strings.xml`，不硬编码中文/英文到布局或代码中
- 尺寸值用 `dp`（布局）和 `sp`（文字），不用 `px`

## Compose 规范
- Composable 函数大驼峰命名
- 状态提升：Screen 级 Composable 接收 `state: UiState` 和事件回调
- 使用 `remember` / `rememberSaveable` 管理局部状态
- 副作用用 `LaunchedEffect` / `DisposableEffect`
- 列表用 `LazyColumn`/`LazyRow`，设置 `key` 参数
- Preview 函数独立提供测试数据，不依赖 ViewModel
- Material 3 组件库（`androidx.compose.material3`）

## 主题与样式
- 颜色、字体、形状统一在 Theme 中定义
- 深色模式支持：使用 `?attr/colorXxx` 引用主题属性
- 不在代码中硬编码颜色值

## 性能
- 避免在 `onDraw` / Compose recomposition 中创建对象
- 图片加载统一使用 Glide（传统 View）或 Coil（Compose）
- 大图使用 `BitmapFactory.Options.inSampleSize` 降采样
- RecyclerView 设置 `setHasFixedSize(true)`（固定尺寸列表）