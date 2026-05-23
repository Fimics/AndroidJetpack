# /permission - 添加运行时权限处理

为指定页面添加 Android 运行时权限请求逻辑。

## 步骤

1. 确认需要的权限列表（如 CAMERA、LOCATION、BLUETOOTH 等）
2. 在 `AndroidManifest.xml` 中添加 `<uses-permission>` 声明
3. 在 Activity/Fragment 中添加权限请求代码：
   - 使用 `ActivityResultContracts.RequestMultiplePermissions()`
   - 检查权限是否已授予
   - 处理用户拒绝（显示说明对话框）
   - 处理"不再询问"（引导到设置页面）
4. 针对不同 API 级别处理：
   - Android 12+ (API 31)：蓝牙权限变更（BLUETOOTH_SCAN/CONNECT/ADVERTISE）
   - Android 13+ (API 33)：通知权限 POST_NOTIFICATIONS、细化媒体权限
   - Android 14+ (API 34)：前台服务类型声明

## 权限分组速查

- **位置**：ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION, ACCESS_BACKGROUND_LOCATION
- **蓝牙**：BLUETOOTH_SCAN, BLUETOOTH_CONNECT, BLUETOOTH_ADVERTISE (API 31+)
- **相机**：CAMERA
- **存储**：READ_MEDIA_IMAGES, READ_MEDIA_VIDEO, READ_MEDIA_AUDIO (API 33+)
- **通知**：POST_NOTIFICATIONS (API 33+)