# 测试规范

## 测试分类
- **单元测试**（`test/`）：纯逻辑测试，不依赖 Android 框架，JUnit 4
- **设备端测试**（`androidTest/`）：需要 Android 环境，UI 测试、数据库测试

## 单元测试规范
- 测试类命名：`XxxTest`（被测试类名 + Test）
- 测试方法命名：`` `should return empty list when no data`() `` 或 `test_xxx_when_yyy_then_zzz`
- 每个测试方法只验证一个行为
- 使用 AAA 模式：Arrange（准备）→ Act（执行）→ Assert（断言）
- ViewModel 测试用 `kotlinx-coroutines-test` 的 `runTest` + `TestDispatcher`
- Repository 测试 mock 数据源，验证数据流转逻辑

## Room 数据库测试
- 使用 `Room.inMemoryDatabaseBuilder` 创建内存数据库
- 在 `androidTest/` 中编写，需要 Android Context
- 测试所有 Dao 的 CRUD 操作和复杂查询

## 运行命令
```bash
# 单元测试
./gradlew :<模块>:testDebugUnitTest

# 指定测试类
./gradlew :<模块>:testDebugUnitTest --tests "com.mic.XxxTest"

# 设备端测试
./gradlew :<模块>:connectedDebugAndroidTest
```

## 测试覆盖优先级
1. ViewModel 业务逻辑（最重要）
2. Repository 数据流转
3. 工具类 / 扩展函数
4. Room Dao 查询
5. UI 交互测试（优先级最低，维护成本高）