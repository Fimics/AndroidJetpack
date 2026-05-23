# /build - 构建指定模块

根据用户指定的模块名构建 debug APK。如果未指定模块，构建主 app 模块。

## 步骤

1. 确认要构建的模块（默认 `:app`）
2. 运行 `./gradlew :<模块路径>:assembleDebug`
3. 如果构建失败，分析错误日志并给出修复建议
4. 构建成功后，输出 APK 路径

## 常用模块路径

- `:app` — 主应用
- `:app-compose` — Compose 示例
- `:app-navigation` — Navigation 示例
- `:ble:ble-client` — BLE 客户端
- `:ble:ble-server` — BLE 服务端
- `:di:hilt` — Hilt 示例
- `:di:dagger` — Dagger 示例
- `:netconfig` — 网络配网
