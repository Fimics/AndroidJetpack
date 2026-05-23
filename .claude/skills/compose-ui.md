# /compose-ui - 创建 Compose UI 组件

快速生成 Jetpack Compose UI 组件代码。

## 步骤

1. 确认组件需求：组件名称、功能描述、交互方式
2. 生成 Composable 函数，遵循以下规范：
   - 使用 Material 3 组件库
   - 状态提升（State Hoisting）：参数接收状态和事件回调
   - 提供 `@Preview` 函数
   - 使用 `Modifier` 参数支持外部自定义
3. 如果涉及列表，使用 `LazyColumn` / `LazyRow`
4. 如果涉及导航，使用 NavHost + composable 路由
5. 将文件放到 `app-compose` 模块或用户指定模块中

## 常用模式

- 表单页面：TextField + Button + 验证逻辑
- 列表详情：LazyColumn + 点击跳转详情
- 底部导航：Scaffold + BottomNavigation + NavHost
- 对话框：AlertDialog / BottomSheet