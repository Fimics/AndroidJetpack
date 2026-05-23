# Code Review

对最近修改的代码进行全面审查，检查规范合规性、性能问题、安全漏洞，并建议测试用例。

## 步骤

### 1. 收集变更范围

- 运行 `git diff --name-only HEAD~1` 和 `git diff --cached --name-only` 获取最近修改的文件列表
- 如果没有提交变更，运行 `git diff --name-only` 和 `git status` 获取未提交的修改
- 读取所有变更文件的完整内容和 diff

### 2. 项目规范检查

逐文件检查以下规范（参考 `.claude/rules/` 中的规则）：

**Kotlin 风格**
- 命名是否符合规范（大驼峰类名、小驼峰函数/变量、全大写常量）
- 是否使用了 `!!`、`var`（可用 `val` 时）、Java Stream API
- 是否正确使用作用域函数（let/apply/run/also/with）
- 集合操作是否使用 Kotlin 惯用写法

**架构合规**
- 是否遵循 MVVM 分层（View 不含业务逻辑、ViewModel 不持有 View 引用）
- UI 状态是否用 `data class UiState` + `StateFlow` 管理
- Repository 是否返回 `Flow<T>` 或 `suspend` 函数

**协程使用**
- 是否使用正确的作用域（viewModelScope/lifecycleScope，禁止 GlobalScope）
- 是否使用正确的调度器（IO 操作在 Dispatchers.IO）
- Flow 收集是否在 `repeatOnLifecycle` 中
- 异常是否被正确处理（非裸 catch、非吞异常）

**Gradle 构建**
- 新依赖是否在 `libs.versions.toml` 中声明（禁止硬编码版本号）
- 依赖作用域是否正确（implementation vs api vs kapt）

**生命周期**
- 是否有内存泄漏风险（静态持有 Context、未注销回调、Handler 泄漏）
- 资源是否在对应生命周期中释放

### 3. 性能问题识别

检查以下性能隐患：

- **主线程阻塞**：IO/网络操作、大数据解析、同步锁是否在主线程
- **内存问题**：大 Bitmap 未降采样、集合持续增长无清理、大对象缓存无上限
- **布局性能**：层级嵌套过深（>3层）、`notifyDataSetChanged` 替代 DiffUtil、过度绘制
- **协程泄漏**：未取消的 Job、callbackFlow 未 awaitClose、无限循环 Flow 未约束作用域
- **数据库**：Room 查询未加索引的大表、查询未指定列（SELECT *）、主线程查询
- **网络**：重复请求无缓存、大文件同步下载、未设置超时

### 4. 安全漏洞检查

检查以下安全问题：

- **硬编码敏感信息**：密码、API Key、Token 出现在代码或资源文件中
- **日志泄漏**：Log/KLog 输出用户密码、token、个人信息
- **数据存储**：敏感数据使用明文 SharedPreferences（应用 EncryptedSharedPreferences）
- **网络安全**：HTTP 明文通信、缺少证书验证、URL 中携带敏感参数
- **组件暴露**：Activity/Service/BroadcastReceiver/ContentProvider 的 `exported` 属性不当
- **WebView**：不必要的 JavaScript 启用、未限制 URL 白名单
- **BLE 安全**（如涉及）：明文传输凭证、未验证设备身份、未清理内存中的密码
- **Intent 安全**：隐式 Intent 未验证来源、未校验 extras 数据类型和范围
- **SQL 注入**：Room `@Query` 中拼接字符串（应用参数绑定 `:param`）

### 5. 测试建议

根据变更内容，建议需要补充的测试用例：

- **ViewModel 测试**：新增/修改的 ViewModel 方法是否有对应单元测试
  - 初始状态验证
  - 正常数据流验证
  - 异常/边界情况验证
  - 状态变更时序验证
- **Repository 测试**：数据流转逻辑、缓存策略、异常处理
- **工具类测试**：新增的扩展函数、数据转换函数
- **Room Dao 测试**：新增/修改的查询方法
- **UI 测试**（仅关键交互）：核心业务流程的端到端测试

给出具体的测试方法签名和核心断言建议。

### 6. 输出报告

按以下格式输出审查结果：

```
## Review 报告

### 变更概述
- 修改文件数：N
- 新增文件数：N
- 涉及模块：xxx, yyy

### 规范问题 (N 项)
| 严重程度 | 文件:行号 | 问题描述 | 修复建议 |
|----------|-----------|----------|----------|
| Error    | Xxx.kt:42 | ...      | ...      |
| Warning  | Yyy.kt:15 | ...      | ...      |

### 性能问题 (N 项)
| 严重程度 | 文件:行号 | 问题描述 | 修复建议 |
|----------|-----------|----------|----------|

### 安全问题 (N 项)
| 严重程度 | 文件:行号 | 问题描述 | 修复建议 |
|----------|-----------|----------|----------|

### 建议补充的测试用例
1. `XxxViewModelTest`
   - `should xxx when yyy`
   - `should handle error when zzz`
2. ...

### 总评
- 整体质量：优/良/中/差
- 是否可合并：是/否（附理由）
```

如果没有发现任何问题，明确说明代码质量良好。对每个问题都提供可直接应用的修复代码。