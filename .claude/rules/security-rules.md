# 安全开发规范

## 数据安全
- 敏感数据（密码、token、密钥）不硬编码在代码中
- API Key 放在 `local.properties`（已在 .gitignore 中）或 BuildConfig
- 用户凭证使用 `EncryptedSharedPreferences` 或 `DataStore` + 加密
- 日志中不输出敏感信息（密码、token、个人数据）
- Release 构建关闭调试日志

## 网络安全
- HTTPS only（`android:usesCleartextTraffic="false"`）
- 证书固定（Certificate Pinning）用于关键 API
- 不在 URL 参数中传递敏感数据
- 网络响应不信任，做输入验证

## 权限最小化
- 只申请必要权限
- 运行时权限在使用前申请，不在启动时全部请求
- `uses-permission` 添加 `android:maxSdkVersion` 限定不再需要的版本

## BLE 安全（本项目特有）
- 生产环境必须启用 BLE 配对（Bonding）
- WiFi 密码传输应加密（当前是明文 POC，需改进）
- 验证连接设备身份，防止中间人攻击
- 配网完成后清除内存中的凭证

## 代码安全
- 不使用 `MODE_WORLD_READABLE` / `MODE_WORLD_WRITEABLE`
- ContentProvider 设置 `android:exported="false"`（除非需要对外暴露）
- WebView 禁用 `setJavaScriptEnabled` 除非必要，并限制 URL 白名单
- Intent 接收方验证来源，不信任 extras 数据