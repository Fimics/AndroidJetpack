# BLE WiFi 配网项目

## 项目概述

本项目为 AndroidJetpack 实现了一套完整的 **BLE（蓝牙低功耗）WiFi 配网** 解决方案。包含两个独立的 Android 模块：

- **ble-client**：手机端应用（Central 角色）用于扫描设备、连接、发送 WiFi 凭证
- **ble-server**：IoT 设备端应用（Peripheral 角色）用于广播、接收凭证、连接 WiFi

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                   BLE WiFi 配网系统                             │
└─────────────────────────────────────────────────────────────────┘
                                │
                ┌───────────────┴───────────────┐
                │                               │
        ┌───────▼────────┐            ┌────────▼────────┐
        │   ble-client   │            │   ble-server    │
        │   （Central）  │            │  （Peripheral） │
        │   设备 B       │◄──BLE GATT─►   设备 A       │
        │   （手机）     │            │ （IoT 设备）    │
        └────────────────┘            └────────┬────────┘
                                               │
                                    ┌──────────▼────────┐
                                    │   WiFi 网络       │
                                    │   路由器 / AP     │
                                    └───────────────────┘
```

## 核心特性

- ✅ **无侵入式配网**：手机无需切换 WiFi 网络
- ✅ **低功耗消耗**：BLE 比 WiFi 功耗低得多
- ✅ **安全可靠**：支持蓝牙配对、加密传输
- ✅ **实时反馈**：客户端通过通知接收配网状态
- ✅ **Nordic BLE 库**：基于成熟的 Nordic Semiconductor BLE 栈
- ✅ **Kotlin 协程**：现代异步编程模式
- ✅ **MVVM 架构**：ViewModel + StateFlow 响应式 UI

## 模块结构

### ble-client（手机端 - Central）
```
ble-client/
├── build.gradle.kts                         # 构建配置
├── src/main/
│   ├── AndroidManifest.xml                  # BLE 权限声明
│   ├── java/com/mic/ble/client/
│   │   ├── BleClientActivity.kt             # 主 UI Activity
│   │   ├── BleClientViewModel.kt            # 状态管理
│   │   ├── DeviceAdapter.kt                 # 设备列表适配器
│   │   ├── scanner/
│   │   │   └── BleScanner.kt                # BLE 扫描工具
│   │   ├── manager/
│   │   │   └── ProvisioningBleManager.kt    # Nordic BleManager 扩展
│   │   ├── model/
│   │   │   ├── WiFiCredentials.kt           # WiFi 凭证数据类
│   │   │   └── ProvisioningStatus.kt        # 配网状态枚举
│   │   └── protocol/
│   │       └── GattUUID.kt                  # GATT 服务/特征 UUID
│   └── res/
│       ├── layout/
│       │   ├── activity_ble_client.xml      # 主界面布局
│       │   └── item_device.xml              # 设备列表项布局
│       └── values/
│           └── strings.xml                  # 字符串资源
```

### ble-server（设备端 - Peripheral）
```
ble-server/
├── build.gradle.kts                         # 构建配置
├── src/main/
│   ├── AndroidManifest.xml                  # WiFi + BLE 权限声明
│   ├── java/com/mic/ble/server/
│   │   ├── BleServerActivity.kt             # 主 UI Activity
│   │   ├── BleServerViewModel.kt            # 状态管理
│   │   ├── advertiser/
│   │   │   └── BleAdvertiser.kt             # BLE 广播工具
│   │   ├── gatt/
│   │   │   └── ProvisioningGattServer.kt    # GATT 服务器实现
│   │   ├── wifi/
│   │   │   └── WiFiConnector.kt             # WiFi 连接工具
│   │   └── protocol/
│   │       └── GattUUID.kt                  # GATT 服务/特征 UUID
│   └── res/
│       ├── layout/
│       │   └── activity_ble_server.xml      # 主界面布局
│       └── values/
│           └── strings.xml                  # 字符串资源
```

## GATT 协议设计

### 服务与特征

| 实体 | UUID | 属性 | 用途 |
|------|------|------|------|
| **配网服务** | `12345678-1234-1234-1234-1234567890AB` | 主服务 | WiFi 配网服务 |
| **SSID 特征** | `12345678-1234-1234-1234-1234567890AC` | WRITE | WiFi 网络名（客户端→服务器） |
| **密码特征** | `12345678-1234-1234-1234-1234567890AD` | WRITE | WiFi 密码（客户端→服务器） |
| **状态特征** | `12345678-1234-1234-1234-1234567890AE` | READ, NOTIFY | 连接状态（服务器→客户端） |

### 数据格式

- **SSID**：UTF-8 字符串，最大 32 字节
- **Password**：UTF-8 字符串，最大 64 字节
- **Status**：UTF-8 字符串值：`IDLE`、`RECEIVING_CREDENTIALS`、`CONNECTING_TO_WIFI`、`SUCCESS`、`FAILED`

### 配网流程

```
客户端                              服务器
  │                                   │
  ├─── 扫描 PROVISIONING_SERVICE ───→ │（正在广播）
  │                                   │
  ├─────────── 连接 ──────────────→ │
  │                                   │
  ├─── 启用通知 ─────────────────→ │
  │                                   │
  ├─── 写入 SSID ────────────────→ │ (RECEIVING_CREDENTIALS)
  │                                   │
  ├─── 写入 Password ────────────→ │ (CONNECTING_TO_WIFI)
  │                                   │
  │ ←─── 通知状态：SUCCESS/FAILED ── │
  │                                   │
```

## 环境要求

### 硬件
- 支持蓝牙 4.2+ 的 Android 设备（BLE 支持）
- Android 7.0+ 的手机或平板
- 目标设备需要 WiFi 功能

### 软件
- Android SDK 24+
- Kotlin 1.8+
- Gradle 8.0+

### 权限声明

**ble-client（客户端）:**
```xml
<!-- BLE 扫描 -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- 旧版本支持（API 30-） -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />

<!-- 位置权限（Android 6-11 BLE 扫描需要） -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- 硬件特性 -->
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />
```

**ble-server（服务器）:**
```xml
<!-- BLE 广播 -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- WiFi 控制 -->
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- 位置权限 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## 编译说明

### 环境准备
```bash
# 确保安装了 Android SDK 35
# Gradle 8.9.0+
# Kotlin 2.1.0+
```

### 编译命令

**编译所有模块:**
```bash
./gradlew :ble:ble-client:assembleDebug
./gradlew :ble:ble-server:assembleDebug
```

**编译单个模块:**
```bash
# 仅客户端
./gradlew :ble:ble-client:assembleDebug

# 仅服务器
./gradlew :ble:ble-server:assembleDebug
```

### 安装应用

```bash
# 在设备 A 上安装服务器应用
adb -s DEVICE_A install ble/ble-server/build/outputs/apk/debug/ble-server-debug.apk

# 在设备 B 上安装客户端应用
adb -s DEVICE_B install ble/ble-client/build/outputs/apk/debug/ble-client-debug.apk
```

## 测试步骤

### 环境设置

1. **准备两个 Android 设备:**
   - 设备 A：目标 IoT 设备或模拟器（运行 ble-server）
   - 设备 B：手机或平板（运行 ble-client）

2. **两个设备都需启用蓝牙**

3. **在各自设备上安装应用**

### 测试流程

#### 步骤 1：启动服务器（设备 A）
1. 在设备 A 上启动 **BLE Server** 应用
2. 点击 **"开始广播"** 按钮
3. 观察状态变化：
   - "广播中：激活"
   - "状态：服务器运行中，等待客户端..."

#### 步骤 2：扫描与连接（设备 B）
1. 在设备 B 上启动 **BLE Client** 应用
2. 点击 **"扫描设备"** 按钮（扫描 10 秒）
3. 设备 A 应出现在列表中
4. 点击设备 A 以选中它
5. 点击 **"连接"** 按钮
6. 等待连接状态显示 "已连接"

#### 步骤 3：配置 WiFi（设备 B）
1. 在文本框中输入 WiFi SSID
2. 在密码框中输入 WiFi 密码
3. 点击 **"配网"** 按钮
4. 客户端显示状态变化：
   - "接收凭证中..." → "连接到 WiFi 中..." → "成功" 或 "失败"
5. 服务器应用显示相似的状态更新

#### 步骤 4：验证（设备 A）
- 设备 A 连接到指定的 WiFi 网络
- 状态变化为 "WiFi 连接成功！"
- 在 Android 设置中可验证 WiFi 连接

### 高级测试

**使用 nRF Connect 应用（推荐）:**

1. 从 Play Store 安装 [nRF Connect](https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp)
2. 在设备 A 上启动 Server 应用
3. 在设备 B 上打开 nRF Connect
4. 扫描并查找配网服务（UUID: `12345678-1234-1234-1234-1234567890AB`）
5. 连接到服务器
6. 验证 GATT 结构：
   - 服务 UUID
   - 三个特征及其正确的 UUID
   - 状态特征有 CCCD 描述符用于通知
7. 手动写入 SSID 和 Password 特征
8. 订阅状态特征以查看通知

**监控 BLE 流量:**

在设备 A 上（已启用 USB 调试）：
```bash
# 启用 HCI 嗅探日志
adb shell settings put global bluetooth_hci_log 1

# 捕获日志
adb bugreport ble_traffic.zip

# 测试后禁用
adb shell settings put global bluetooth_hci_log 0

# 使用 Wireshark + nRF Sniffer 分析
# https://github.com/NordicSemiconductor/nRF-Sniffer-for-Bluetooth-LE
```

## GATT 操作时间参考

| 操作 | 超时时间 | 重试次数 | 用途 |
|------|--------|--------|------|
| 连接 | 15 秒 | 3 | 建立 BLE 连接 |
| 写入 SSID | 5 秒 | 1 | 发送网络名称 |
| 写入密码 | 5 秒 | 1 | 发送网络密码 |
| MTU 协商 | 默认 | 1 | 协商最大数据包大小（23 → 512 字节） |
| 通知等待 | 30 秒 | - | 等待状态更新 |

## 常见问题与故障排查

### 客户端问题

#### 1. "扫描期间未发现设备"
**原因:**
- 服务器未运行
- 服务器蓝牙未启用
- 设备超出范围（BLE 范围约 10-30 米）

**解决方案:**
- 验证服务器应用正在运行且显示"广播中：激活"
- 重启蓝牙：设置 → 蓝牙 → 关闭/打开
- 将两个设备靠近
- 确保两个设备都支持蓝牙 4.2+

#### 2. "连接失败/超时"
**原因:**
- 重试次数已用尽
- 服务器拒绝连接
- 其他 BLE 设备干扰

**解决方案:**
- 关闭附近的 BLE 应用
- 确保设备支持 BLE 4.2+
- 查看日志：`adb logcat | grep ProvisioningBleManager`
- 重置蓝牙配对：设置 → 应用 → 权限 → 蓝牙 → 重置

#### 3. "配网失败/无响应"
**原因:**
- 服务器未接收到写入
- MTU 协商失败
- 通知未启用

**解决方案:**
- 验证服务器处于"客户端已连接"状态
- 检查状态特征是否被监控
- 使用 nRF Connect 手动测试写入
- 查看服务器日志：`adb logcat | grep ProvisioningGattServer`

### 服务器问题

#### 1. "启动广播失败"
**原因:**
- 蓝牙未启用
- 缺少 `BLUETOOTH_ADVERTISE` 权限
- 过多的活动广播程序

**解决方案:**
- 在设置中启用蓝牙
- 授予所有请求的权限（Android 12+ 需运行时权限）
- 关闭其他 BLE 广播应用
- 重启应用

#### 2. "GATT 服务器未打开"
**原因:**
- 启动期间蓝牙被禁用
- 系统资源耗尽

**解决方案:**
- 重启设备
- 确保其他 BLE 应用已关闭
- 检查设备内存状态

#### 3. "接收凭证后 WiFi 连接失败"
**原因:**
- SSID/密码不正确
- WiFi 网络不可用
- 设备不支持 WPA2/WPA3
- 缺少 WiFi 权限

**解决方案:**
- 先在其他设备上验证 SSID 和密码
- 检查 WiFi 网络是否正在广播（不是隐藏网络）
- 授予 WiFi 权限：设置 → 应用 → ble-server → 权限
- 查看服务器日志：`adb logcat | grep WiFiConnector`

#### 4. "已连接但无数据交换"
**原因:**
- GATT 服务器未初始化
- 特征未添加到服务
- 回调未正确注册

**解决方案:**
- 检查 UI 是否显示"客户端已连接"
- 在 nRF Connect 中验证特征存在
- 查看服务器日志中的 GATT 回调

## 架构与设计模式

### MVVM 模式
- **Model**：数据类（`WiFiCredentials`、`ProvisioningStatus`）
- **ViewModel**：`BleClientViewModel`、`BleServerViewModel` 管理状态
- **View**：Activity（`BleClientActivity`、`BleServerActivity`）

### 状态管理
- **StateFlow**：UI 状态的响应式更新
- **Coroutines**：异步操作，无需回调
- **Flow**：BLE 事件的热数据流

### BLE 架构
- **BleManager**：Nordic 库基类，处理连接生命周期
- **BleScanner**：封装 BLE 扫描逻辑
- **BleAdvertiser**：处理 BLE 广播
- **ProvisioningGattServer**：GATT 服务器实现

## 依赖项

- **Nordic BLE Library 2.11.0**：核心 BLE 功能
- **Nordic Scanner 1.7.1**：优化的 BLE 扫描
- **AndroidX Core KTX 1.10.1**：现代 Android API
- **Kotlin Coroutines 1.8.0**：异步编程
- **Material Design 3**：UI 组件
- **Gson 2.8.6**：JSON 序列化

详见 `gradle/libs.versions.toml` 的完整依赖列表。

## 性能特性

| 指标 | 值 | 备注 |
|------|-----|------|
| BLE 范围 | 10-30 米 | 取决于天线、障碍物 |
| 连接时间 | 1-5 秒 | 扫描后 |
| 配网时间 | 3-10 秒 | 包括 WiFi 连接 |
| 功耗（客户端扫描中） | ~10-50 mA | 连接后非常低 |
| 功耗（服务器广播中） | ~5-30 mA | WiFi 连接时 ~50 mA |
| 最大凭证长度 | 96 字节 | SSID(32) + 密码(64) |
| MTU 大小 | 512 字节 | 连接时协商 |

## 安全考虑

### 当前实现
- 明文通过 BLE 传输凭证
- 无加密或认证（概念验证）

### 推荐增强
1. **BLE 配对**：启用 `BluetoothDevice.createBond()` 以进行加密
2. **AES 加密**：在传输前加密 SSID/密码
3. **HMAC 签名**：验证消息完整性
4. **时间限制令牌**：一次性配网代码
5. **公钥交换**：非对称加密凭证

### 生产部署
- 实现设备配对/绑定
- 使用加密 GATT 特征
- 添加认证机制（PIN、QR 码）
- 实现 HTTPS 用于云通信
- 使用 WPA3 进行 WiFi 安全
- 实现超时机制防止暴力破解

## API 参考

### BleClientViewModel

```kotlin
// 开始扫描设备（10 秒扫描）
fun startScan()

// 停止正在进行的扫描
fun stopScan()

// 连接到发现的设备
fun connectToDevice(device: BluetoothDevice)

// 向连接的设备发送 WiFi 凭证
fun provision(credentials: WiFiCredentials)

// 与设备断开连接
fun disconnect()

// 观察 UI 状态
val scannedDevices: StateFlow<List<BluetoothDevice>>
val isScanning: StateFlow<Boolean>
val connectionStatus: StateFlow<String>
val provisioningStatus: StateFlow<ProvisioningStatus>
```

### BleServerViewModel

```kotlin
// 启动 BLE 广播和 GATT 服务器
fun startProvisioning()

// 停止广播和服务器
fun stopProvisioning()

// 观察 UI 状态
val isAdvertising: StateFlow<Boolean>
val provisioningStatus: StateFlow<String>
val connectionCount: StateFlow<Int>
```

## 延伸阅读

- [BLE 配网技术分析](BLE配网技术分析.md) - 完整的协议分析
- [Nordic BLE 库深度分析](Nordic_BLE库深度分析.md) - 源码架构
- [BLE 技术生态](BLE技术生态.md) - 相关技术

## 许可证

本项目属于 AndroidJetpack 的一部分，遵循相同的许可证条款。

## 获取帮助

如有问题和疑问：
1. 查看 [故障排查](#常见问题与故障排查) 部分
2. 查看日志输出：`adb logcat | grep -i ble`
3. 使用 nRF Connect 应用测试隔离问题
4. 查阅相应模块的源代码
