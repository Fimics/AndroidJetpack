# /test - 运行测试

运行指定模块的单元测试或 Android 设备端测试。

## 步骤

1. 确认模块和测试类型（单元测试 or 设备端测试）
2. 单元测试：`./gradlew :<模块>:testDebugUnitTest`
3. 设备端测试：`./gradlew :<模块>:connectedDebugAndroidTest`
4. 如果指定了具体测试类：追加 `--tests "com.mic.XxxTest"`
5. 分析测试结果，报告失败用例及原因
