# Nordic BLE 库 - 深度分析

## 目录
1. [库概述](#库概述)
2. [架构与设计模式](#架构与设计模式)
3. [请求队列机制](#请求队列机制)
4. [BleManager 生命周期](#blemanager-生命周期)
5. [GATT 回调处理](#gatt-回调处理)
6. [通知数据管道](#通知数据管道)
7. [线程模型](#线程模型)
8. [绑定和配对](#绑定和配对)
9. [MTU 和数据分片](#mtu-和数据分片)
10. [Kotlin 扩展 (ble-ktx)](#kotlin-扩展-ble-ktx)
11. [服务器端实现](#服务器端实现)

---

## 库概述

### Maven 坐标

```gradle
dependencies {
    // 核心 BLE 库（Java）
    implementation("no.nordicsemi.android:ble:2.11.0")

    // Kotlin 扩展
    implementation("no.nordicsemi.android:ble-ktx:2.11.0")

    // BLE 扫描工具
    implementation("no.nordicsemi.android.support.v18:scanner:1.7.1")
}
```

### 模块分解

| 模块 | 用途 | 语言 |
|------|------|------|
| **ble** | 核心 BleManager、GATT 回调、请求队列 | Java |
| **ble-ktx** | Kotlin suspend/Flow 扩展 | Kotlin |
| **ble-livedata** | LiveData 集成（遗留） | Kotlin |
| **ble-common** | 公共工具和接口 | Kotlin |
| **scanner** | 优化的 BLE 扫描（Android 5.0+） | Java |

### 版本策略

- **2.11.0**（最新，2024）：稳定，新项目推荐
- **2.10.x**：前一版本，安全反向移植
- **1.x**：已弃用，不要使用

---

## 架构与设计模式

### 类层级结构

```
BleManager（抽象）
├─ 属性：
│  ├─ bluetoothGatt: BluetoothGatt?
│  ├─ handler: Handler
│  ├─ callbacks: BleManagerGattCallback
│  └─ requestQueue: Deque<Request>
│
├─ 核心方法：
│  ├─ connect(device): ConnectRequest
│  ├─ read(uuid): ReadRequest
│  ├─ write(uuid, data): WriteRequest
│  ├─ enableNotifications(uuid): Request
│  └─ disableNotifications(uuid): Request
│
└─ 抽象方法（在子类中实现）：
   ├─ getGattCallback(): BleManagerGattCallback
   ├─ getLogSession(): LogSession?
   └─ log(profile, message)

BleManagerGattCallback（抽象）
├─ onServicesDiscovered(success)
├─ onDeviceReady()
├─ initialize()
├─ isRequiredServiceSupported(gatt): Boolean
└─ onDeviceDisconnected()

Request（抽象）
├─ enqueued: Boolean
├─ started: Boolean
├─ finished: Boolean
│
├─ 方法：
│  ├─ enqueue(): Request
│  ├─ done(callback): Request
│  ├─ fail(callback): Request
│  └─ then(nextRequest): Request
│
├─ 子类：
│  ├─ TimeoutableRequest
│  │  ├─ ConnectRequest
│  │  ├─ ReadRequest
│  │  ├─ WriteRequest
│  │  ├─ WaitForValueChangedRequest
│  │  └─ MtuRequest
│  │
│  └─ SimpleRequest
│     ├─ BondRequest
│     ├─ SleepRequest
│     └─ CacheRequest
```

### 使用的设计模式

#### 1. 模板方法模式
```kotlin
// BleManager 定义框架，子类填充细节
abstract class BleManager {
    abstract fun getGattCallback(): BleManagerGattCallback

    // 模板方法
    private fun onServicesDiscovered() {
        val callback = getGattCallback()
        callback.onServicesDiscovered()
        // 继续设备就绪流程
    }
}

// 你的实现
class ProvisioningBleManager(context: Context) : BleManager(context) {
    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt): Boolean {
            // 检查你的特定服务/特征
        }

        override fun initialize() {
            // 设置通知、请求 MTU 等
        }
    }
}
```

#### 2. 请求队列（生产者-消费者）
```kotlin
// 序列化所有 BLE 操作
// FIFO：先入先出
// 操作永不并行

manager.requestMtu(512)    // 入队
    .done { ... }           // 回调
    .enqueue()              // 开始执行

manager.write(...)          // 等待 MTU 完成
    .enqueue()
```

#### 3. 构建者模式
```kotlin
// 请求配置的流畅 API
connect(device)
    .retry(3, 200)         // 3 次重试，间隔 200ms
    .timeout(15_000)       // 15 秒超时
    .useAutoConnect(false)  // 直接连接
    .done { ... }          // 成功回调
    .fail { ... }          // 失败回调
    .then { ... }          // 链接下一个请求
    .enqueue()             // 执行
```

#### 4. 策略模式（回调和过滤器）
```kotlin
// DataMerger：如何合并通知数据包
setNotificationCallback(characteristic)
    .with(JsonMerger())          // 合并 JSON 对象
    // 或
    .with(JsonMerger("data"))    // 合并特定 JSON 密钥
    .with(object : DataMerger { ... })  // 自定义合并器
```

#### 5. 观察者模式（BluetoothGattCallback）
```kotlin
// BleManager 观察系统 BLE 事件
class BleManagerHandler : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt, status, newState) {
        // 内部处理 + 请求队列执行
    }

    override fun onCharacteristicChanged(gatt, char) {
        // 路由到通知回调
    }
}
```

---

## 请求队列机制

### 核心概念

**问题**：Android 的 BluetoothGatt 回调非阻塞，允许多个并发操作。这导致：
- 错误：蓝牙栈拒绝重叠操作
- 竞态条件：不可预测的排序
- 崩溃：某些设备系统崩溃

**解决方案**：Nordic 的请求队列序列化所有操作。

### 队列结构

```kotlin
// 两个独立队列
private val initQueue: Deque<Request> = LinkedList()     // 仅初始化
private val requestQueue: Deque<Request> = LinkedList()  // 常规操作

// 流程：
// 1. 所有初始请求在 initQueue 上串行执行
// 2. 然后设备就绪
// 3. 所有常规请求在 requestQueue 上串行执行
```

### 请求生命周期

```
请求状态机：

[待处理]
   │
   ├─ enqueue()
   │
[已入队]
   │
   ├─ 由队列处理器启动
   │
[已启动]
   │
   ├─ 收到回调
   │ (onCharacteristicRead、onCharacteristicWrite 等)
   │
[已完成]
   │
   ├─ 调用 done() 或 fail() 回调
   │
[完成]
   │
   └─ 出队下一个请求
```

### 请求类型（40+）

```kotlin
// 连接
CONNECT              // 建立 BLE 连接
AUTO_CONNECT         // 后台重连
DISCONNECT           // 关闭连接

// 发现
DISCOVER_SERVICES    // 查找 GATT 服务
REQUEST_MTU          // 协商 MTU 大小
REQUEST_PHY          // 协商 PHY (2M/Coded)

// GATT 操作
READ                 // 读特征值
WRITE                // 写入响应
WRITE_NO_RESPONSE    // 无响应写入
SIGNED_WRITE         // 签名写入（无配对）

// 通知
ENABLE_NOTIFICATIONS
DISABLE_NOTIFICATIONS
WAIT_FOR_NOTIFICATION
WAIT_FOR_VALUE_CHANGED

// 绑定
CREATE_BOND
REMOVE_BOND
ENSURE_BOND

// 描述符
READ_DESCRIPTOR
WRITE_DESCRIPTOR

// 工具
SLEEP                // 操作间延迟
CACHE                // 管理 GATT 缓存

// 服务器（如果使用 BleServerManager）
SEND_NOTIFICATION
SEND_INDICATION
WAIT_FOR_WRITE
WAIT_FOR_READ
```

### 队列处理循环

```kotlin
// 简化内部逻辑
private fun processQueue() {
    if (currentRequest != null) {
        // 等待当前完成
        return
    }

    val nextRequest = requestQueue.pollFirst() ?: return

    try {
        currentRequest = nextRequest
        nextRequest.execute()  // 异步操作
        // onXxxCallback 完成时会调用 processQueue()
    } catch (e: Exception) {
        nextRequest.notifyError(e)
        currentRequest = null
        processQueue()  // 继续下一个
    }
}

// 操作完成时
override fun onCharacteristicRead(gatt, char, status) {
    currentRequest?.onCharacteristicRead(gatt, char, status)
    currentRequest = null
    processQueue()  // 出队下一个
}
```

### 实际使用

```kotlin
// 所有这些都被序列化和执行：
manager.requestMtu(512)          // 第 1 个
    .retry(1, 100)
    .enqueue()

manager.enableNotifications(statusChar)  // 第 2 个（等待 MTU）
    .retry(1, 100)
    .enqueue()

manager.write(ssidChar, ssidData)  // 第 3 个
    .retry(1, 100)
    .timeout(5_000)
    .enqueue()

manager.write(passwordChar, passwordData)  // 第 4 个
    .retry(1, 100)
    .timeout(5_000)
    .enqueue()

// 执行时间表：
// T+0ms:   发送 MTU 请求
// T+50ms:  收到 MTU 响应 → 发送启用通知
// T+100ms: 通知已启用 → 发送写入 SSID
// T+150ms: 已写入 SSID → 发送写入密码
// T+200ms: 已写入密码 → 完成
```

---

## BleManager 生命周期

### 连接流程（10 步）

```
步骤 1：设备已选择
┌─────────────────────┐
│ 用户点击设备        │
│ manager.connect()   │
└────────┬────────────┘
         │
步骤 2：连接已启动
│ ├─ stopScan()
│ ├─ connectGatt() 调用
│ └─ 等待 onConnectionStateChange()
│
步骤 3：已连接
│ ├─ 连接状态 = CONNECTED
│ ├─ 检查绑定
│ │  └─ 如未绑定：createBond() + 等待
│ └─ 启动服务发现
│
步骤 4：服务已发现
│ ├─ onServicesDiscovered(status=0)
│ ├─ 调用 isRequiredServiceSupported()
│ │  └─ 必须返回 true 或断开连接
│ └─ 如果支持：继续，否则：断开连接
│
步骤 5：等待服务缓存
│ ├─ 已绑定设备：1600ms 延迟（缓存可靠性）
│ ├─ 未绑定：300ms 延迟（更快）
│ └─ 防止：过早读取新服务
│
步骤 6：初始化
│ ├─ 调用 BleManagerGattCallback.initialize()
│ ├─ 设置通知
│ ├─ 请求 MTU（23→512）
│ └─ 其他初始化操作
│
步骤 7：设备就绪
│ ├─ 调用 onDeviceReady()
│ └─ 应用现在可使用设备
│
步骤 8-10：操作和断开连接
│ ├─ 应用读/写特征
│ ├─ 应用调用 disconnect()
│ └─ onDeviceDisconnected() 清理
```

### 代码演练

```kotlin
// 步骤 1：发起连接
manager.connect(device)
    .retry(3, 200)         // 最多重试 3 次
    .timeout(15_000)       // 最多 15 秒
    .useAutoConnect(false)  // 直接连接（非自动重连）
    .done { device -> /* 成功 */ }
    .fail { device, status -> /* 失败 */ }
    .enqueue()

// 内部（步骤 2-7）：
private fun onConnectionStateChange(newState) {
    when (newState) {
        STATE_CONNECTED -> {
            // 步骤 3：已连接
            checkBondingState()  // 如未绑定，createBond()
            startServiceDiscovery()
        }
    }
}

private fun onServicesDiscovered(status) {
    // 步骤 4
    val isSupported = callback.isRequiredServiceSupported(gatt)
    if (!isSupported) {
        disconnect()
        return
    }

    // 步骤 5：等待缓存
    val delayMs = if (device.bondState == BOND_BONDED) 1600 else 300
    handler.postDelayed({
        // 步骤 6：初始化
        callback.initialize()
        // 步骤 7：就绪
        callback.onDeviceReady()
    }, delayMs)
}
```

### 连接参数

```kotlin
// useAutoConnect() 行为：
connect(device)
    .useAutoConnect(false)  // 直接：立即连接
                            // 超时：最多 30 秒
                            // 功耗：更高消耗
                            // 用途：首次连接

connect(device)
    .useAutoConnect(true)   // 自动：在范围内时排队连接
                            // 超时：无限期
                            // 功耗：功耗更低
                            // 用途：后台重连
```

---

## GATT 回调处理

### BluetoothGattCallback 拦截

```kotlin
// 系统回调
interface BluetoothGattCallback {
    fun onConnectionStateChange(gatt, status, newState)
    fun onServicesDiscovered(gatt, status)
    fun onCharacteristicRead(gatt, char, status)
    fun onCharacteristicWrite(gatt, char, status)
    fun onCharacteristicChanged(gatt, char)
    fun onDescriptorRead(gatt, desc, status)
    fun onDescriptorWrite(gatt, desc, status)
    fun onReadRemoteRssi(gatt, rssi, status)
    fun onMtuChanged(gatt, mtu, status)
    fun onPhyRead(gatt, txPhy, rxPhy, status)
    fun onPhyUpdate(gatt, txPhy, rxPhy, status)
    fun onServiceChanged(gatt)
    fun onExecuteWrite(gatt, status)
}

// Nordic 的包装器（BleManagerHandler）
class BleManagerHandler : BluetoothGattCallback() {
    private var currentRequest: Request? = null
    private val requestQueue: Deque<Request> = LinkedList()

    override fun onCharacteristicRead(gatt, char, status) {
        if (status == GATT_SUCCESS) {
            // 处理值
            valueCallbacks[char.uuid]?.onDataReceived(char.value)
        }

        // 通知当前请求
        currentRequest?.onCharacteristicRead(gatt, char, status)

        // 继续队列
        currentRequest = null
        processQueue()
    }

    // ... 其他回调类似
}
```

### 错误代码映射

```kotlin
// 原始 GATT 状态码（0x00 到 0xFF）映射到错误：

0x00 GATT_SUCCESS
0x01 GATT_INVALID_OFFSET
0x02 GATT_INVALID_ATTRIBUTE_LENGTH
0x03 GATT_INVALID_ATTRIBUTE_VALUE_LENGTH
0x04 GATT_INSUFFICIENT_AUTHENTICATION
0x05 GATT_REQUEST_NOT_SUPPORTED
0x06 GATT_INVALID_OFFSET
0x07 GATT_INSUFFICIENT_ENCRYPTION
0x08 GATT_INVALID_ATTRIBUTE_VALUE_LENGTH
0x0C GATT_INSUFFICIENT_ENCRYPTION_KEY_SIZE
0x0D GATT_INVALID_ATTRIBUTE_VALUE_LENGTH
0x0E GATT_INVALID_OFFSET
0x0F GATT_INSUFFICIENT_ENCRYPTION

// Android 特定错误（负值）：
-1     GATT_ERROR（泛型）
-2     GATT_CONNECTION_CONGESTED
-3     GATT_SERVICE_NOT_FOUND

// 设备断开连接：
133    连接丢失（Nexus 5、6.0）
134    连接已关闭

// Nordic 映射到原因代码：
REASON_SUCCESS
REASON_CANCELLED
REASON_TIMEOUT
REASON_DEVICE_DISCONNECTED
REASON_NOT_SUPPORTED
REASON_REQUEST_FAILED
REASON_AUTH_ERROR
```

### 状态变量

```kotlin
class BleManagerHandler {
    // 内部状态
    var servicesDiscovered = false      // 在 onServicesDiscovered 后
    var mtu = 23                        // 默认，onMtuChanged 后
    var bondState = BOND_NONE           // 绑定检查后
    var connectionState = STATE_DISCONNECTED
    var lock = Mutex()                  // 保护并发访问

    // 请求跟踪
    var currentRequest: Request? = null
    var requestQueue: Deque<Request>
    var initQueue: Deque<Request>

    // 回调
    var valueChangedCallback: ValueChangedCallback? = null
    var notificationCallbacks: Map<UUID, ValueChangedCallback>
}
```

---

## 通知数据管道

### 回调链

```
原始 BLE 通知
        │
        ▼
┌───────────────────────────────────────┐
│ onCharacteristicChanged()             │
│ (由 BluetoothGattCallback 调用)       │
└────────┬────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────┐
│ 过滤器（可选）                         │
│ if (char.uuid == myUuid && ...)       │
└────────┬────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────┐
│ 数据合并器（可选）                     │
│ JsonMerger、PacketMerger 等          │
│ 重新组装分片数据                       │
└────────┬────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────┐
│ 最终回调                               │
│ ValueChangedCallback.onDataReceived() │
│ 应用接收完整值                         │
└───────────────────────────────────────┘
```

### 回调注册

```kotlin
// 设置通知回调
val callback: ValueChangedCallback = object : ValueChangedCallback() {
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val value = data.getStringValue(0, StandardCharsets.UTF_8)
        Log.d(TAG, "收到：$value")
    }
}

// 为特定特征注册
setNotificationCallback(characteristic)
    .with(callback)  // 直接回调
    .with(JsonMerger())  // 先合并 JSON 数据包
    .enqueue()
```

### 数据合并器

```kotlin
// 无合并器（原始数据通过）
setNotificationCallback(statusChar)
    .with(callback)
    .enqueue()

// JSON 对象合并
setNotificationCallback(jsonDataChar)
    .with(JsonMerger())  // 跨数据包合并 {...}
    .with(callback)
    .enqueue()

// 合并特定 JSON 字段
setNotificationCallback(jsonDataChar)
    .with(JsonMerger("temperature"))  // 仅合并此字段
    .with(callback)
    .enqueue()

// 自定义合并器
setNotificationCallback(char)
    .with(object : DataMerger() {
        override fun merge(current: Data, next: Data): Data? {
            // 组合两个数据包
            val combined = current + next
            // 如不完整返回 null，完整返回 Data
            return if (isComplete(combined)) combined else null
        }
    })
    .with(callback)
    .enqueue()
```

### 阻塞等待通知

```kotlin
// 用于请求-响应模式
WaitForValueChangedRequest()
    .timeout(5_000)
    .with(JsonMerger())
    .with(object : ValueChangedCallback() {
        override fun onDataReceived(device, data) {
            val response = data.getStringValue(0)
            processResponse(response)
        }
    })
    .enqueue()

// Kotlin suspend 版本
val data = manager.waitForNotification(statusChar)
    .timeout(5_000)
    .suspend()

val value = data.getStringValue(0, StandardCharsets.UTF_8)
Log.d(TAG, "收到：$value")
```

---

## 线程模型

### 线程执行

```kotlin
// 默认行为：
蓝牙回调线程（系统 BLE 线程）
    ├─ BluetoothGattCallback 在此执行
    ├─ 非 UI 线程，资源有限
    ├─ 必须非阻塞
    └─ 必须不执行 UI 操作

// Nordic 的处理器：
回调处理器（可配置）
    ├─ 默认：主线程
    ├─ 可设置为自定义 Looper
    ├─ 允许回调中 UI 更新
    ├─ 允许后台处理
```

### 配置处理器

```kotlin
// 构造函数
val manager = ProvisioningBleManager(context)

// 默认：主线程处理器
// 由 Android 框架自动设置

// 自定义处理器（后台）
val backgroundHandler = Handler(
    Looper.getMainLooper().thread.also {
        // 创建自定义 looper
    }
)

// 在 BleManager 上设置（仅 Java API，Kotlin 中无）
// manager.handler = backgroundHandler
```

### Kotlin 扩展行为

```kotlin
// ble-ktx 行为（重要！）
// 设置 handler = null

// 效果：回调在 BLE 系统线程上运行
// 优势：
// - 无主线程阻塞
// - 回调更快
// 劣势：
// - 无法执行 UI 操作
// - 必须快速

// 你的代码必须处理线程：
manager.statusFlow()  // 返回 Flow<Data>
    .flowOn(Dispatchers.Main)  // 切换到主线程
    .collect { data ->
        // 安全更新 UI
    }
```

### 线程安全

```kotlin
// 请求队列是线程安全的
manager.write(char, data)  // 安全，来自任何线程
    .enqueue()

// GATT 回调被同步
class BleManagerHandler {
    private val lock = ReentrantLock()

    override fun onCharacteristicChanged(gatt, char) {
        lock.withLock {
            // 对共享状态的访问受保护
        }
    }
}

// 但特征值读取必须在回调中
// 不要做：
manager.read(char)
    .done { /* 成功 */ }
    .enqueue()
val value = char.value  // ❌ 还未设置！

// 要做：
manager.read(char)
    .done { device, value ->
        val actualValue = value  // ✓ 在此设置
    }
    .enqueue()
```

---

## 绑定和配对

### 绑定状态

```kotlin
// 绑定是持久配对 + 存储加密密钥

BOND_NONE = 10           // 未绑定
BOND_BONDING = 11        // 绑定进行中
BOND_BONDED = 12         // 已绑定（配对已存储）

device.bondState         // 读取当前状态
device.createBond()      // 启动绑定
device.removeBond()      // 清除存储的绑定
```

### BLE 配对方法

```kotlin
// 1. Just Works（无用户交互）
// - 无 PIN 需要
// - IoT 最常见
// - 易受 MITM 攻击
// → 如果 BLE 范围短，可接受配网

// 2. Passkey Entry（用户输入 PIN）
// - 更安全
// - 用户在两个设备上看到 6 位 PIN
// - 在无显示设备上用户输入 PIN

// 3. 数字比较（接受/拒绝）
// - 在两个设备上显示 6 位数字
// - 用户确认数字匹配
// - 最快的安全配对

// 4. 带外 (NFC、QR 等)
// - 最高安全性
// - 通过二级通道的预共享密钥
// - 需要 NFC 或 QR 扫描

// 5. LE 安全连接
// - 使用 ECDH 密钥交换
// - 抗 MITM 攻击
// - BLE 4.2+ 支持
```

### 请求绑定

```kotlin
// 1. 自动绑定请求
val bondReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
            val device = intent.getParcelableExtra<BluetoothDevice>(
                BluetoothDevice.EXTRA_DEVICE
            )

            // 接受配对
            device?.setPairingConfirmation(true)
        }
    }
}

val filter = IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
registerReceiver(bondReceiver, filter)

// 2. 启动绑定（Nordic）
manager.ensureBond()
    .done { /* 成功 */ }
    .fail { /* 失败 */ }
    .enqueue()
```

### 配网流程中的绑定

```kotlin
connect(device)
    .retry(3, 200)
    .timeout(15_000)
    .done { /* 已连接 */ }
    .enqueue()

ensureBond()
    .done { /* 已绑定 */ }
    .enqueue()

requestMtu(512)
    .done { /* MTU 已协商 */ }
    .enqueue()

// 全部顺序执行
```

---

## MTU 和数据分片

### MTU（最大传输单元）

```
L2CAP/ATT MTU

默认：23 字节
最大：517 字节（实际：512）

写入操作的分解：
┌────────────────────────────────┐
│  23 字节默认 MTU              │
├────────────────────────────────┤
│ 操作码 (1) | 句柄 (2) | ...   │ 头：3 字节
├────────────────────────────────┤
│ 数据                       | CRC│ 数据：20 字节
└────────────────────────────────┘

使用 MTU 512：
┌────────────────────────────────┐
│  512 字节协商 MTU             │
├────────────────────────────────┤
│ 头（3 字节）                    │
├────────────────────────────────┤
│ 数据（509 字节）                │
└────────────────────────────────┘
```

### MTU 协商

```kotlin
// 请求更大 MTU
manager.requestMtu(512)
    .done { mtu ->
        Log.d(TAG, "MTU 设置为：$mtu")  // 可能小于请求的
    }
    .fail { device, status ->
        Log.e(TAG, "MTU 请求失败：$status")
    }
    .enqueue()

// 结果：
// - 成功：调用 onMtuChanged
// - 失败：保持默认 23 字节
// - 如 MTU 保持 23，不要失败，某些设备不支持
```

### 数据分段

```kotlin
// 写入大数据（> MTU - 3）：
manager.write(largeDataChar, Data(1000 bytes))
    .enqueue()

// 内部：自动分段
// MTU 512：
// 分段 1：509 字节
// 分段 2：491 字节
// 每分段后等待 ACK

// 无需手动干预
// 库透明处理分段
```

### 可靠写入（已弃用模式）

```kotlin
// 旧风格：可靠写入带回显
ReliableWriteRequest()
    .write(char1, data1)  // 发送，获得回显
    .write(char2, data2)  // 发送，获得回显
    .done { /* 全部成功写入 */ }
    .enqueue()

// 不推荐：
// - 更慢（回显往返）
// - 仅用于有缺陷固件的设备
// - 新设计使用普通 Write
```

---

## Kotlin 扩展 (ble-ktx)

### Suspend 函数

```kotlin
// 传统回调风格
manager.write(char, data)
    .done { Log.d(TAG, "已写入") }
    .fail { _, reason -> Log.e(TAG, "失败：$reason") }
    .enqueue()

// Kotlin suspend 风格
try {
    manager.write(char, data).suspend()
    Log.d(TAG, "成功写入")
} catch (e: Exception) {
    Log.e(TAG, "失败：${e.message}")
}
```

### Flow 集成

```kotlin
// 作为 Flow 读取（热流）
manager.statusChar.asFlow(manager)
    .collect { data ->
        val value = data.getStringValue(0)
        Log.d(TAG, "状态：$value")
    }

// 状态作为 Flow
manager.stateAsFlow()  // BleManager.STATE_*
    .collect { state ->
        when (state) {
            BleManager.STATE_CONNECTED -> { /* ... */ }
            BleManager.STATE_DISCONNECTED -> { /* ... */ }
        }
    }

// 绑定状态作为 Flow
manager.bondingStateAsFlow()
    .collect { bondState ->
        when (bondState) {
            BluetoothDevice.BOND_BONDED -> { /* ... */ }
        }
    }

// 请求-响应 Flow
manager.readBatteryLevel()
    .asResponseFlow<Int>()
    .collect { level ->
        Log.d(TAG, "电池：$level%")
    }
```

### 错误映射

```kotlin
// BLE 错误映射到 Kotlin 异常：

REASON_SUCCESS
    → 成功完成

REASON_CANCELLED
    → 抛出 CancellationException

REASON_TIMEOUT
    → 抛出 TimeoutException

REASON_DEVICE_DISCONNECTED
    → 抛出 DeviceDisconnectedException

REASON_NOT_SUPPORTED
    → 抛出 RequestNotSupportedException

REASON_REQUEST_FAILED
    → 抛出 RequestFailedException

// 使用：
try {
    manager.enableNotifications(char).suspend()
} catch (e: TimeoutException) {
    Log.e(TAG, "通知启用超时")
} catch (e: CancellationException) {
    Log.i(TAG, "通知启用被用户取消")
}
```

### Flow 操作符

```kotlin
// 内置帮助器
manager.writeAndWait(char, data)  // 写入后等待通知

manager.readAndWait(char)         // 读取后订阅通知

manager.asValidResponseFlow<String>()  // 仅发出非空响应

// 与转换
manager.stateAsFlow()
    .map { state -> stateToString(state) }
    .distinctUntilChanged()
    .collect { statusStr ->
        updateUI(statusStr)
    }

// 与过滤
manager.statusFlow()
    .filter { it.contains("SUCCESS") }
    .take(1)
    .collect { /* 仅第一个成功 */ }
```

### 作用域和生命周期

```kotlin
// 协程作用域绑定
lifecycleScope.launch {
    try {
        manager.write(char, data).suspend()
    } catch (e: Exception) {
        // Activity 销毁时自动取消
    }
}

// Flow 集合
viewModelScope.launch {
    manager.stateAsFlow()
        .collect { state ->
            // 使用 viewModel 作用域取消
        }
}
```

---

## 服务器端实现

### BleServerManager

```kotlin
// 创建服务器
val serverManager = BleServerManager(context)

// 用 DSL 定义服务
serverManager.server(serviceUuid)
    .service {
        characteristic(charUuid, properties, permissions) {
            onWrite { device, value ->
                Log.d(TAG, "收到：${String(value)}")
            }
        }
        characteristic(statusUuid, PROPERTY_READ or PROPERTY_NOTIFY, ...) {
            onRead { device ->
                // 返回当前值
                deviceInfo.toString().toByteArray()
            }
        }
        descriptor(cccdUuid, ...)  // NOTIFY/INDICATE 自动添加
    }

// 启动服务器
serverManager.open()
    .done { /* 监听中 */ }
    .fail { reason -> /* 失败 */ }
    .enqueue()

// 发送通知
serverManager.sendNotification(device, char, data)
    .done { /* 已发送 */ }
    .enqueue()

// 关闭服务器
serverManager.close()
```

### 每客户端共享特征

```kotlin
// 共享（默认）
characteristic(uuid, PROPERTY_NOTIFY, ...)
    // 所有已连接客户端接收相同通知

// 每客户端
sharedCharacteristic(uuid, PROPERTY_NOTIFY, ...)
    // 每个客户端获得独立回调

// 示例：
sharedCharacteristic(statusUuid, ...) {
    onNotified { device ->
        // 客户端禁用通知时调用
        Log.d(TAG, "客户端 ${device.address} 禁用了通知")
    }
}
```

### 等待客户端操作

```kotlin
// 等待客户端写入
WaitForWriteRequest(char)
    .done { device, data ->
        val value = String(data)
        Log.d(TAG, "客户端写入：$value")
    }
    .timeout(30_000)
    .enqueue()

// 等待读取
WaitForReadRequest(char)
    .done { device ->
        Log.d(TAG, "客户端读取中...")
        // 通过 onRead 回调返回数据
    }
    .enqueue()
```

---

## 最佳实践

### 错误恢复

```kotlin
// 始终重试操作
write(char, data)
    .retry(2, 100)  // 2 次重试，100ms 间隔
    .timeout(5_000)
    .fail { device, reason ->
        when (reason) {
            REASON_TIMEOUT -> { /* 重试逻辑 */ }
            REASON_DEVICE_DISCONNECTED -> { /* 重连 */ }
        }
    }
    .enqueue()
```

### 资源清理

```kotlin
override fun onCleared() {
    // ViewModel 清理
    manager.disconnect().enqueue()
    manager.close()
}

override fun onDestroy() {
    // Activity 清理
    unregisterReceiver(bondReceiver)
    super.onDestroy()
}
```

### 超时策略

```kotlin
// 连接：激进（想快速知道）
connect(device).timeout(15_000)

// 写入：中等（BLE 比 BT 慢）
write(char, data).timeout(5_000)

// 通知等待：长（可能是用户触发）
waitForNotification().timeout(30_000)

// 扫描：很长（用户可能不靠近）
startScan(10_000 或更长)
```

### 测试

```kotlin
// 使用内存模拟
class MockBleManager : BleManager() {
    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt) = true
        override fun initialize() { /* */ }
    }

    fun simulateNotification(data: Data) {
        // 直接触发回调
        valueChangedCallback?.onDataReceived(mockDevice, data)
    }
}

// 单元测试
@Test
fun testProvisioning() {
    val manager = MockBleManager()
    // 模拟写入、通知
    // 断言状态变化
}
```

---

## 相关资源

- [项目 README](README.md) - 概述
- [BLE 配网技术分析](BLE配网技术分析.md) - BLE 概念
- [BLE 技术生态](BLE技术生态.md) - 相关标准

## 参考资源

- [Nordic BLE 库 GitHub](https://github.com/NordicSemiconductor/Android-BLE-Library)
- [Android BluetoothGatt 文档](https://developer.android.com/reference/android/bluetooth/BluetoothGatt)
- [BLE 规范 (SIG)](https://www.bluetooth.com/specifications/specs/)
