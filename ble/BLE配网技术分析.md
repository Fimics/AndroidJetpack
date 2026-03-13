# BLE WiFi 配网 - 技术分析

## 目录
1. [BLE 基础概念](#ble-基础概念)
2. [WiFi 配网方法对比](#wifi-配网方法对比)
3. [GATT 协议设计](#gatt-协议设计)
4. [Android BLE 权限演变](#android-ble-权限演变)
5. [技术栈选择理由](#技术栈选择理由)
6. [数据流与时序](#数据流与时序)
7. [安全架构](#安全架构)

---

## BLE 基础概念

### 协议栈层级

BLE 跨越多个协议层，每层有其具体职责：

```
┌─────────────────────────────────────────────┐
│            应用层                            │
│  (你的应用：BleClientActivity、ViewModel)   │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│        GATT (通用属性配置文件)               │
│  服务、特征、描述符                          │
│  读、写、通知、指示操作                      │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│   ATT (属性协议)                             │
│  请求/响应、句柄寻址                        │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│  L2CAP (逻辑链路控制和适配)                 │
│  分段、重组、多路复用                       │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│    链路层 (蓝牙 LE)                          │
│  广播、扫描、连接状态                       │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│    物理层 (射频)                             │
│  2.4 GHz ISM 频段、40 个信道                │
└─────────────────────────────────────────────┘
```

### GAP (通用访问配置文件)
定义设备发现和连接的角色和过程：

| 概念 | 定义 |
|------|------|
| **广播器** | 发送广告数据包（无连接） |
| **观察者** | 监听广告数据包 |
| **中心设备** | 发起连接（可连接多个设备） |
| **外围设备** | 接受连接（通常单连接） |

我们的实现：
- **ble-client** → 中心设备角色（扫描和发起连接）
- **ble-server** → 外围设备角色（广播和接受连接）

### GATT (通用属性配置文件)
定义分层属性结构：

```
服务 (UUID)
  ├─ 特征 (UUID)
  │   ├─ 属性 (READ、WRITE、NOTIFY、INDICATE 等)
  │   ├─ 描述符 (CCCD 等)
  │   └─ 值 (BLE 5.x 中 0-512 字节)
  ├─ 特征
  └─ ...

服务
  ├─ 特征
  └─ ...
```

### 服务、特征、描述符结构

```
配网服务 (12345678-1234-1234-1234-1234567890AB)
│
├─ SSID 特征 (12345678-1234-1234-1234-1234567890AC)
│   └─ 属性: WRITE
│
├─ 密码特征 (12345678-1234-1234-1234-1234567890AD)
│   └─ 属性: WRITE
│
└─ 状态特征 (12345678-1234-1234-1234-1234567890AE)
    ├─ 属性: READ、NOTIFY
    └─ 客户端特征配置描述符 (CCCD)
        └─ UUID: 00002902-0000-1000-8000-00805f9b34fb
        └─ 允许客户端启用/禁用通知
```

### 特征属性

| 属性 | 方向 | 响应 | 用途 |
|------|------|------|------|
| **READ** | 服务器→客户端 | 必需 | 获取当前值 |
| **WRITE** | 客户端→服务器 | 必需 | 发送小数据 |
| **WRITE_NO_RESPONSE** | 客户端→服务器 | 无 | 快速写入（无确认） |
| **NOTIFY** | 服务器→客户端 | 无 | 异步状态更新 |
| **INDICATE** | 服务器→客户端 | 必需 | 可靠的异步更新 |
| **SIGNED_WRITE** | 客户端→服务器 | 无 | 无配对的加密 |

我们的设计：
- SSID/密码：WRITE（客户端发送，无需响应）
- 状态：READ + NOTIFY（客户端订阅更新）

---

## WiFi 配网方法对比

### 概览表

| 方法 | 技术 | 优点 | 缺点 | 范围 | 速度 |
|------|------|------|------|------|------|
| **BLE 配网**（此项目） | 蓝牙 LE | 无需切换网络、低功耗、安全配对选项 | 需要 BLE 硬件 | 10-30m | 1-5s |
| **SoftAP** | WiFi AP | 直观、无需蓝牙 | 用户需切换网络 | 10-30m | 5-10s |
| **SmartConfig (ESP-Touch)** | UDP 广播 | 对用户透明、无切换 | 不稳定、墙体穿透差 | 5-20m | 10-30s |
| **ZeroConf / mDNS** | UDP 多播 | 自动发现、简单 | 需要同一网络 | 仅 LAN | 2-5s |
| **BLE Mesh 配网** | 蓝牙 Mesh | 一对多配网 | 协议复杂 | 10-30m | 5-20s |
| **NFC** | NFC/RFID | 轻触式、无扫描 | 范围极短、特殊硬件 | 5-10cm | 1-2s |
| **二维码** | 视觉 | 快速、离线可用 | 需手动步骤 | - | 可变 |

### 详细对比

#### BLE 配网（我们的实现）
```
优势：
✓ 非侵入式 - 手机保持原 WiFi 连接
✓ 低功耗（BLE << WiFi）
✓ 标准协议（GATT）
✓ 良好的安全模型（配对、绑定、加密）
✓ 广泛的设备支持
✓ WiFi 移交后离线工作

劣势：
✗ 需要两个设备都支持蓝牙
✗ 需要 Android 7.0+（BLE 标准，API 24+）
✗ 无法配网仅 WiFi 设备（无头）
✗ 凭证数据大小受限（MTU ~512 字节）
✗ Android 12+ 需要位置权限
```

#### SoftAP
```
优势：
✓ 实现简单
✓ 仅需 WiFi，无特殊硬件
✓ 连接快速

劣势：
✗ 用户需手动切换 WiFi 网络
✗ 切换后需再次切换回去
✗ 网络名/密码可见
✗ 干扰设备正常运行
✗ 消费产品用户体验差
```

#### SmartConfig (ESP-Touch、Espressif)
```
优势：
✓ 对用户透明
✓ 用户保持当前 WiFi
✓ 实现简单

劣势：
✗ 非标准（供应商专有）
✗ 2.4GHz 拥挤时不可靠
✗ 无法传输大数据
✗ 需要 ESP32/ESP8266 硬件
✗ 专利/许可问题
```

#### ZeroConf/mDNS
```
优势：
✓ 自动发现
✓ 无需预共享密码

劣势：
✗ 需要同一网络段
✗ 不适合初始配网
✗ WiFi 上多播不可靠
✗ 无网络连接无法工作
```

#### BLE Mesh 配网（SIG 标准）
```
优势：
✓ 一对多配网
✓ 标准协议（SIG Mesh）
✓ 出色的安全模型

劣势：
✗ 协议复杂（不轻量级）
✗ 简单配网过度设计
✗ 需要 Mesh 兼容硬件
✗ 开发周期长
```

### BLE 配网决策矩阵

选择 BLE 配网的条件：
- ✅ 设备具有蓝牙功能
- ✅ 用户拥有智能手机（手机作为配网工具）
- ✅ 非侵入式用户体验至关重要
- ✅ 功耗很重要（IoT 设备）
- ✅ 需要安全/加密
- ✅ 初始配网后可离线工作

不选择 BLE 的条件：
- ❌ 目标设备无蓝牙
- ❌ 用户仅有功能手机
- ❌ 需要高速大容量数据传输
- ❌ 必须在 BLE 干扰环境中工作
- ❌ 延迟必须 < 100ms

---

## GATT 协议设计

### 服务设计

#### 自定义 vs 标准 UUID

**自定义 UUID（本项目）：**
```
格式：xxxxxxxx-1234-1234-1234-xxxxxxxxxxxx
示例：12345678-1234-1234-1234-1234567890AB

优势：
✓ 每个公司/产品唯一
✓ 完全控制服务设计
✓ 无需 SIG 注册

劣势：
✗ 通用工具无法识别
✗ 必须记录协议
```

**标准 SIG UUID：**
```
格式：0000xxxx-0000-1000-8000-00805f9b34fb
示例：180A = 设备信息服务（SIG）

优势：
✓ 工具/库广泛知晓
✓ 可互操作性好
✓ 文档少

劣势：
✗ 必须符合规范
✗ 无法自定义
```

对于 WiFi 配网，自定义 UUID 合适，因为不存在标准。

### 特征设计决策

#### SSID 和密码：WRITE 属性

```
理由：
- 客户端需要 WRITE 凭证到服务器
- WRITE 属性 = 双向流
  - 客户端发送：要写入的值
  - 服务器响应：成功/失败状态
- WRITE_NO_RESPONSE 会更快但风险更高
  - 无服务器确认
  - 写入失败时客户端不知道

数据模型：
- SSID：UTF-8 字符串，最大 32 字节（WiFi 标准）
- 密码：UTF-8 字符串，最大 63 字节（WiFi 标准）
- 两者都使用 WRITE 属性以可靠性
```

#### 状态：READ + NOTIFY

```
理由：
- 服务器需异步发送状态
- READ：客户端可立即查询当前状态
- NOTIFY：服务器推送状态变化给客户端
  - 避免持续轮询
  - 客户端通过 CCCD（客户端特征配置描述符）订阅
  - 客户端断连时服务器不排队通知

状态值：
- IDLE：等待凭证
- RECEIVING_CREDENTIALS：已获取 SSID/密码
- CONNECTING_TO_WIFI：尝试连接
- SUCCESS：WiFi 连接成功
- FAILED：连接失败
```

### 描述符：客户端特征配置描述符 (CCCD)

```
UUID：00002902-0000-1000-8000-00805f9b34fb
所有 NOTIFY/INDICATE 特征的标准 SIG 描述符

值：
- 0x0000 = 通知禁用
- 0x0001 = 通知启用
- 0x0002 = 指示启用
- 0x0003 = 两者启用

由 GATT 服务器实现自动添加
允许客户端订阅/取消订阅通知
```

---

## Android BLE 权限演变

### API 23 (Android 6.0) - 原始粗粒度模型

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- 运行时权限请求 -->
if (ContextCompat.checkSelfPermission(context, BLUETOOTH)
    != PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(...)
}
```

### API 29 (Android 10) - 更精细的粒度

```xml
<!-- API 29 引入：-->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- BLE 扫描仍然需要 -->
```

### API 31 (Android 12) - 主要重组

```xml
<!-- BLUETOOTH 分解为具体操作 -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- 仍需旧版用于 API 30- -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />

<!-- 位置仍需用于扫描 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### Android 12+ 运行时权限请求

```kotlin
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    val permissions = arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
    requestPermissions(permissions, REQUEST_CODE)
}
```

### 我们的实现

**AndroidManifest.xml (ble-client)：**
```xml
<!-- 覆盖 API 23-30 和 31+ -->
<uses-permission android:name="android.permission.BLUETOOTH"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**BleClientActivity.kt (运行时请求)：**
```kotlin
val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
    arrayOf(
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
} else {
    arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.ACCESS_FINE_LOCATION
    )
}
permissionLauncher.launch(permissions)
```

### 关键权限类

| 权限 | API | 用途 | 要求 |
|------|-----|------|------|
| BLUETOOTH | 4+ | 旧版 BLE 访问 | API 31+ 已移除 |
| BLUETOOTH_ADMIN | 4+ | 旧版管理员访问 | API 31+ 已移除 |
| BLUETOOTH_SCAN | 31+ | BLE 扫描 | API 31+ 运行时 |
| BLUETOOTH_CONNECT | 31+ | BLE 连接 | API 31+ 运行时 |
| BLUETOOTH_ADVERTISE | 31+ | BLE 广播 | API 31+ 运行时 |
| ACCESS_FINE_LOCATION | 6+ | WiFi/BLE 位置信息 | API 6+ 运行时 |
| ACCESS_COARSE_LOCATION | 6+ | 网络位置 | FINE 的替代品 |

---

## 技术栈选择理由

### 为什么选择 Nordic BLE 库？

#### 与替代方案的对比

| 方面 | Nordic BLE | Android 原生 BluetoothGatt | RxAndroidBle |
|------|-----------|---------|------------|
| **学习曲线** | 中等 | 陡峭 | 中等-高 |
| **可靠性** | 非常高 | 有缺陷/分散 | 非常高 |
| **主动维护** | 是（2024+） | 内置 | 社区 |
| **请求队列** | 内置（串行） | 手动同步 | 内置 |
| **错误处理** | 映射错误 | 原始代码 | 良好 |
| **文档** | 良好 | 稀疏 | 中等 |
| **Kotlin 支持** | KTX 扩展 | 有限 | 有限 |
| **示例项目** | 很多 | 少数 | 一些 |
| **回调地狱** | 最小化 | 回调链 | Rx 操作符 |

#### Nordic BLE 2.11.0 特性

```kotlin
// 请求队列（稳定性关键）
manager.requestMtu(512)
    .retry(1, 100)
    .done { status -> Log.d("MTU 协商") }
    .enqueue()

// 自动状态管理
connect(device)
    .retry(3, 200)
    .timeout(15_000)
    .useAutoConnect(false)
    .enqueue()

// 错误映射
.fail { device, status ->
    when (status) {
        BinderError -> // 系统错误
        GattError -> // GATT 协议错误
        RequestError -> // 请求验证错误
    }
}

// Kotlin 协程支持 (ble-ktx)
val mtu = manager.requestMtu(512).suspend()
manager.getStatusFlow().collect { status ->
    // 响应式更新
}
```

### 为什么选择 Kotlin 协程 + Flow 而不是 RxJava？

| 特性 | 协程 + Flow | RxJava 3 |
|------|-----------|----------|
| **学习曲线** | 更容易（顺序代码） | 更陡峭（操作符链） |
| **内存开销** | 更低 | 更高（Observable 对象） |
| **错误处理** | Try-catch | 操作符（catching、errorHandler） |
| **取消** | 内置（suspend） | 手动（Disposable） |
| **标准（Google）** | 官方（Kotlin stdlib） | 第三方 |
| **Android Jetpack** | 完全集成 | 部分（Rx 适配器） |
| **测试** | `runTest { }` | TestScheduler |
| **热流** | SharedFlow | BehaviorSubject、PublishSubject |
| **背压** | 不太相关（UI） | 通过操作符处理 |

我们的选择：
- 协程用于 BLE 操作（顺序、代码更清洁）
- Flow 用于响应式 UI 更新（比 LiveData 更好）
- StateFlow 用于 MVVM 状态管理

### 为什么选择 MVVM 架构？

```
Model 层：
- WiFiCredentials 数据类
- ProvisioningStatus 枚举

ViewModel 层：
- BleClientViewModel
- BleServerViewModel
- 管理 BLE 状态（扫描设备、连接、配网）
- 比 Activity 生命周期更长
- 为 UI 提供 StateFlow

View 层：
- BleClientActivity / BleServerActivity
- 设备列表 RecyclerView
- 观察 ViewModel.StateFlow
- 处理权限请求
- 触发 ViewModel 操作
```

优势：
✓ 关注点分离
✓ 业务逻辑可测试（ViewModel）
✓ 生命周期感知状态
✓ 幸存配置改变
✓ 易于单元测试

---

## 数据流与时序

### 连接流程序列

```
客户端                              服务器
 │                                   │
 ├─ 扫描（10 秒超时）              │ 每 100-200ms 广播
 │                                   │
 ├─ 收到扫描结果                    │
 │                                   │
 ├─ 发起连接 ────────→             ├─ 接受连接
 │ （最多 30 秒超时）               │
 │                                   │
 ├─ 发现 GATT 服务 ────→           ├─ 无需操作
 │ （1-5 秒，设备缓存）             │
 │                                   │
 ├─ 协商 MTU (23→512) ───→          ├─ 响应 MTU
 │ （默认 512 字节）                │
 │                                   │
 ├─ 启用状态通知 ──────→           ├─ 记录通知已启用
 │ （写入 CCCD）                    │
 │                                   │
 └─ 准备好配网                      └─ 准备好服务
```

### 配网数据交换时序

```
时间表：
T+0ms    | 客户端                        | 服务器
T+0      | 写入 SSID                   →|
T+50     |← GATT 写入响应              |
T+100    | 写入密码                    →|
T+150    |← GATT 写入响应              |
T+200    | 启用通知                    | 状态 = 接收凭证
T+300    |← 通知：接收中...            →|
T+500    |                              | 尝试 WiFi 连接
T+1000   |← 通知：连接中...            →|
T+2000   |                              | WiFi 已连接 ✓
T+2100   |← 通知：成功                 →|
T+2200   | 连接成功！断开连接           →|
         |                              | 停止广播
```

### 超时策略

```kotlin
// 连接尝试
connect(device)
    .timeout(15_000)     // 最多 15 秒
    .retry(3, 200)       // 3 次重试，200ms 间隔
    .enqueue()

// SSID 写入
writeSSID(ssid)
    .timeout(5_000)      // 最多 5 秒
    .retry(1, 100)       // 1 次重试，100ms 间隔
    .enqueue()

// 状态订阅
statusFlow.collect { status ->
    // 无限等待（用户可手动断开）
}
```

---

## 安全架构

### 当前实现

**状态**：无加密/认证（概念验证）

```
设备 A（客户端）
└─ SSID："MyNetwork" ──BLE→ 设备 B（服务器）
└─ 密码："pass123" ──BLE→
```

**风险**：如果 BLE 设备被窃听，凭证暴露。

### 推荐的安全增强

#### 1. BLE 配对

```kotlin
// 启用配对
device.createBond()

// 配对变体：
// - Just Works：无带外确认
// - Passkey Entry：用户在设备上输入 PIN
// - Numeric Comparison：显示数字，用户确认匹配
// - Out-of-Band：通过 NFC/QR 的预共享密钥
```

#### 2. AES 加密

```kotlin
// 预共享密钥 (PSK) 加密
private val psk = byteArrayOf(...)  // 16-32 字节

fun encryptCredentials(ssid: String, password: String): ByteArray {
    val plaintext = "$ssid|$password".toByteArray()
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val secretKey = SecretKeySpec(psk, 0, psk.size, "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(plaintext)
}

// 写入加密数据块
writeCharacteristic(GattUUID.CREDENTIALS, encryptedBytes)
    .enqueue()
```

#### 3. HMAC 签名

```kotlin
// 验证消息完整性
fun signMessage(message: ByteArray, secret: ByteArray): ByteArray {
    val hmac = Mac.getInstance("HmacSHA256")
    hmac.init(SecretKeySpec(secret, "HmacSHA256"))
    return hmac.doFinal(message)
}

// 特征：[消息长度 (2 字节)][消息][hmac (32 字节)]
val signature = signMessage(credentials, psk)
val payload = credentials + signature
writeCharacteristic(GattUUID.SIGNED_CREDENTIALS, payload)
    .enqueue()
```

#### 4. 挑战-响应

```kotlin
// 服务器向客户端发送随机挑战
val challenge = Random().nextBytes(16)

// 客户端计算响应
val response = HMAC-SHA256(psk, challenge + credentials)

// 服务器验证
if (received_response == expected_response) {
    // 认证并继续
}
```

#### 5. 时间限制令牌

```kotlin
// 服务器生成配网令牌（有效 5 分钟）
val token = generateToken()
val expiresAt = System.currentTimeMillis() + 5 * 60_1000

// 客户端必须与凭证一起发送令牌
writeCharacteristic(GattUUID.CREDENTIALS,
    token + expiresAt + credentials)
    .enqueue()

// 服务器在接受前验证令牌
if (currentTime < expiresAt && validateToken(token)) {
    acceptProvisioning()
}
```

### 生产安全检查清单

- [ ] 启用 BLE 配对（LE 安全连接、SMP）
- [ ] 至少使用 AES-128 加密凭证数据
- [ ] 实现 HMAC-SHA256 消息认证
- [ ] 为 WiFi 凭证使用 WPA3（不是 WPA2）
- [ ] 实现速率限制（最多每分钟 5 次配网尝试）
- [ ] 为配网模式添加超时（10 分钟后自动禁用）
- [ ] 实现设备认证（证书固定）
- [ ] 任何云通信使用 HTTPS
- [ ] 在嵌入式设备上启用安全引导
- [ ] 启用 WiFi 扫描加密（MAC 随机化）
- [ ] 记录配网尝试以供审计
- [ ] 在用户手册中记录安全态势

---

## 相关资源

- [项目 README](README.md) - 项目概述和快速开始
- [Nordic BLE 库深度分析](Nordic_BLE库深度分析.md) - 库内部
- [BLE 技术生态](BLE技术生态.md) - 相关标准
