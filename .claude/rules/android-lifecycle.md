# Android 生命周期与内存管理规范

## Activity/Fragment 生命周期
- `onCreate` — 初始化 ViewBinding、设置 Observer、注册监听器
- `onStart/onResume` — 开始收集 Flow、注册广播接收器
- `onPause/onStop` — 暂停动画、释放相机/传感器
- `onDestroy` — 解注册（如果未用 lifecycle-aware 组件）
- Fragment 中使用 `viewLifecycleOwner` 而非 `this` 观察数据

## 内存泄漏防护
- ViewModel 不持有 Activity/Fragment/View/Context 引用
- 需要 Context 的地方用 `Application` context（通过 `@ApplicationContext` 注入或 `AndroidViewModel`）
- Handler/Runnable 使用 WeakReference 或在 onDestroy 中 removeCallbacks
- 内部类（非 data class）声明为 `static`（Java）或顶层/伴生（Kotlin）
- 注册的回调必须在对应生命周期中注销
- BLE/蓝牙回调在 onDestroy 中断开连接并清理

## 配置变更处理
- 用 ViewModel 保存 UI 状态（跨配置变更存活）
- 用 `SavedStateHandle` 保存进程被杀后需要恢复的状态
- 不在 `onSaveInstanceState` 中保存大对象

## 后台任务
- 短期异步：协程（viewModelScope / lifecycleScope）
- 需要保证执行：WorkManager
- 精确定时：AlarmManager
- 前台持续运行：Foreground Service（需声明 foregroundServiceType）
- 禁止用 `Thread` / `AsyncTask` / `IntentService`（均已过时）

## 进程优先级意识
- 前台 Activity → 高优先级，不会被杀
- 后台 Service → 可能被系统杀，重要任务用 WorkManager
- 空进程 → 最先被杀，不保存状态在静态变量中