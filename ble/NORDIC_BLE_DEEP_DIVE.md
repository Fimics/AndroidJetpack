# Nordic BLE Library - Deep Dive Analysis

## Table of Contents
1. [Library Overview](#library-overview)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [Request Queue Mechanism](#request-queue-mechanism)
4. [BleManager Lifecycle](#blemanager-lifecycle)
5. [GATT Callback Processing](#gatt-callback-processing)
6. [Notification Data Pipeline](#notification-data-pipeline)
7. [Thread Model](#thread-model)
8. [Bonding & Pairing](#bonding--pairing)
9. [MTU & Data Fragmentation](#mtu--data-fragmentation)
10. [Kotlin Extensions (ble-ktx)](#kotlin-extensions-ble-ktx)
11. [Server-Side Implementation](#server-side-implementation)

---

## Library Overview

### Maven Coordinates

```gradle
dependencies {
    // Core BLE library (Java)
    implementation("no.nordicsemi.android:ble:2.11.0")

    // Kotlin extensions
    implementation("no.nordicsemi.android:ble-ktx:2.11.0")

    // BLE Scanner utility
    implementation("no.nordicsemi.android.support.v18:scanner:1.7.1")
}
```

### Module Breakdown

| Module | Purpose | Language |
|--------|---------|----------|
| **ble** | Core BleManager, GATT callbacks, request queue | Java |
| **ble-ktx** | Kotlin suspend/Flow extensions | Kotlin |
| **ble-livedata** | LiveData integration (legacy) | Kotlin |
| **ble-common** | Common utilities and interfaces | Kotlin |
| **scanner** | Optimized BLE scanning (Android 5.0+) | Java |

### Version Strategy

- **2.11.0** (Latest, 2024): Stable, recommended for new projects
- **2.10.x**: Previous version, security backports
- **1.x**: Deprecated, do not use

---

## Architecture & Design Patterns

### Class Hierarchy

```
BleManager (abstract)
├─ Properties:
│  ├─ bluetoothGatt: BluetoothGatt?
│  ├─ handler: Handler
│  ├─ callbacks: BleManagerGattCallback
│  └─ requestQueue: Deque<Request>
│
├─ Core Methods:
│  ├─ connect(device): ConnectRequest
│  ├─ read(uuid): ReadRequest
│  ├─ write(uuid, data): WriteRequest
│  ├─ enableNotifications(uuid): Request
│  └─ disableNotifications(uuid): Request
│
└─ Abstract Methods (implement in subclass):
   ├─ getGattCallback(): BleManagerGattCallback
   ├─ getLogSession(): LogSession?
   └─ log(profile, message)

BleManagerGattCallback (abstract)
├─ onServicesDiscovered(success)
├─ onDeviceReady()
├─ initialize()
├─ isRequiredServiceSupported(gatt): Boolean
└─ onDeviceDisconnected()

Request (abstract)
├─ enqueued: Boolean
├─ started: Boolean
├─ finished: Boolean
│
├─ Methods:
│  ├─ enqueue(): Request
│  ├─ done(callback): Request
│  ├─ fail(callback): Request
│  └─ then(nextRequest): Request
│
├─ Subclasses:
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

### Design Patterns Used

#### 1. Template Method Pattern
```kotlin
// BleManager defines skeleton, subclass fills in details
abstract class BleManager {
    abstract fun getGattCallback(): BleManagerGattCallback

    // Template method
    private fun onServicesDiscovered() {
        val callback = getGattCallback()
        callback.onServicesDiscovered()
        // Continue with device ready flow
    }
}

// Your implementation
class ProvisioningBleManager(context: Context) : BleManager(context) {
    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt): Boolean {
            // Check your specific service/characteristics
        }

        override fun initialize() {
            // Set up notifications, request MTU, etc.
        }
    }
}
```

#### 2. Request Queue (Producer-Consumer)
```kotlin
// Serializes all BLE operations
// FIFO: First In, First Out
// Operations never run in parallel

manager.requestMtu(512)    // Enqueued
    .done { ... }           // Callback
    .enqueue()              // Start execution

manager.write(...)          // Waits for MTU to complete
    .enqueue()
```

#### 3. Builder Pattern
```kotlin
// Fluent API for request configuration
connect(device)
    .retry(3, 200)         // 3 retries, 200ms between
    .timeout(15_000)       // 15 second timeout
    .useAutoConnect(false)  // Direct connection
    .done { ... }          // Success callback
    .fail { ... }          // Failure callback
    .then { ... }          // Chain next request
    .enqueue()             // Execute
```

#### 4. Strategy Pattern (Callbacks & Filters)
```kotlin
// DataMerger: How to merge notification packets
setNotificationCallback(characteristic)
    .with(JsonMerger())          // Merge JSON objects
    // or
    .with(JsonMerger("data"))    // Merge specific JSON key
    .with(object : DataMerger { ... })  // Custom merger
```

#### 5. Observer Pattern (BluetoothGattCallback)
```kotlin
// BleManager observes system BLE events
class BleManagerHandler : BluetoothGattCallback() {
    override fun onConnectionStateChange(gatt, status, newState) {
        // Internal processing + request queue execution
    }

    override fun onCharacteristicChanged(gatt, char) {
        // Route to notification callbacks
    }
}
```

---

## Request Queue Mechanism

### Core Concept

**Problem**: Android's BluetoothGatt callbacks are non-blocking, allowing multiple concurrent operations. This causes:
- Errors: Bluetooth stack rejects overlapping operations
- Race conditions: Unpredictable ordering
- Crashes: System crashes on some devices

**Solution**: Nordic's request queue serializes all operations.

### Queue Structure

```kotlin
// Two separate queues
private val initQueue: Deque<Request> = LinkedList()     // Initialization only
private val requestQueue: Deque<Request> = LinkedList()  // Regular operations

// Flow:
// 1. All init requests execute serially on initQueue
// 2. Then device ready
// 3. All regular requests execute serially on requestQueue
```

### Request Lifecycle

```
Request State Machine:

[Pending]
   │
   ├─ enqueue()
   │
[Enqueued]
   │
   ├─ Started by queue processor
   │
[Started]
   │
   ├─ Callback received
   │ (onCharacteristicRead, onCharacteristicWrite, etc.)
   │
[Finished]
   │
   ├─ done() or fail() callback invoked
   │
[Done]
   │
   └─ Next request dequeued
```

### Request Types (40+)

```kotlin
// Connection
CONNECT              // Establish BLE connection
AUTO_CONNECT         // Background reconnect
DISCONNECT           // Close connection

// Discovery
DISCOVER_SERVICES    // Find GATT services
REQUEST_MTU          // Negotiate MTU size
REQUEST_PHY          // Negotiate PHY (2M/Coded)

// GATT Operations
READ                 // Read characteristic value
WRITE                // Write with response
WRITE_NO_RESPONSE    // Write without response
SIGNED_WRITE         // Signed write (no pairing needed)

// Notifications
ENABLE_NOTIFICATIONS
DISABLE_NOTIFICATIONS
WAIT_FOR_NOTIFICATION
WAIT_FOR_VALUE_CHANGED

// Bonding
CREATE_BOND
REMOVE_BOND
ENSURE_BOND

// Descriptors
READ_DESCRIPTOR
WRITE_DESCRIPTOR

// Utility
SLEEP                // Delay between operations
CACHE                // Manage GATT cache

// Server (if using BleServerManager)
SEND_NOTIFICATION
SEND_INDICATION
WAIT_FOR_WRITE
WAIT_FOR_READ
```

### Queue Processing Loop

```kotlin
// Simplified internal logic
private fun processQueue() {
    if (currentRequest != null) {
        // Wait for current to finish
        return
    }

    val nextRequest = requestQueue.pollFirst() ?: return

    try {
        currentRequest = nextRequest
        nextRequest.execute()  // Async operation
        // onXxxCallback will call processQueue() when done
    } catch (e: Exception) {
        nextRequest.notifyError(e)
        currentRequest = null
        processQueue()  // Continue to next
    }
}

// When operation completes
override fun onCharacteristicRead(gatt, char, status) {
    currentRequest?.onCharacteristicRead(gatt, char, status)
    currentRequest = null
    processQueue()  // Dequeue next
}
```

### Practical Usage

```kotlin
// All these are queued and executed serially:
manager.requestMtu(512)          // 1st
    .retry(1, 100)
    .enqueue()

manager.enableNotifications(statusChar)  // 2nd (waits for MTU)
    .retry(1, 100)
    .enqueue()

manager.write(ssidChar, ssidData)  // 3rd
    .retry(1, 100)
    .timeout(5_000)
    .enqueue()

manager.write(passwordChar, passwordData)  // 4th
    .retry(1, 100)
    .timeout(5_000)
    .enqueue()

// Execution timeline:
// T+0ms:   MTU request sent
// T+50ms:  MTU response → enable notifications sent
// T+100ms: Notifications enabled → write SSID sent
// T+150ms: SSID written → write Password sent
// T+200ms: Password written → done
```

---

## BleManager Lifecycle

### Connection Flow (10 Steps)

```
Step 1: Device Selected
┌─────────────────────┐
│ User clicks device  │
│ manager.connect()   │
└────────┬────────────┘
         │
Step 2: Connection Initiated
│ ├─ startScan() stops
│ ├─ connectGatt() called
│ └─ Waiting for onConnectionStateChange()
│
Step 3: Connected
│ ├─ Connection state = CONNECTED
│ ├─ Bonding checked
│ │  └─ If not bonded: createBond() + wait
│ └─ Service discovery started
│
Step 4: Services Discovered
│ ├─ onServicesDiscovered(status=0)
│ ├─ isRequiredServiceSupported() called
│ │  └─ Must return true or disconnect
│ └─ If supporting: proceed, else: disconnect
│
Step 5: Wait for Service Cache
│ ├─ Bonded devices: 1600ms delay (cache reliability)
│ ├─ Non-bonded: 300ms delay (faster)
│ └─ Prevents: premature read of fresh services
│
Step 6: Initialization
│ ├─ BleManagerGattCallback.initialize() called
│ ├─ Setup notifications
│ ├─ Request MTU (23→512)
│ └─ Other init operations
│
Step 7: Device Ready
│ ├─ onDeviceReady() called
│ └─ App can now use device
│
Step 8-10: Operation & Disconnect
│ ├─ App reads/writes characteristics
│ ├─ App calls disconnect()
│ └─ onDeviceDisconnected() cleanup
```

### Code Walkthrough

```kotlin
// Step 1: Initiate connection
manager.connect(device)
    .retry(3, 200)         // Retry up to 3 times
    .timeout(15_000)       // Max 15 seconds
    .useAutoConnect(false)  // Direct connection (not auto-reconnect)
    .done { device -> /* Success */ }
    .fail { device, status -> /* Failure */ }
    .enqueue()

// Internally (Steps 2-7):
private fun onConnectionStateChange(newState) {
    when (newState) {
        STATE_CONNECTED -> {
            // Step 3: Connected
            checkBondingState()  // If not bonded, createBond()
            startServiceDiscovery()
        }
    }
}

private fun onServicesDiscovered(status) {
    // Step 4
    val isSupported = callback.isRequiredServiceSupported(gatt)
    if (!isSupported) {
        disconnect()
        return
    }

    // Step 5: Wait for cache
    val delayMs = if (device.bondState == BOND_BONDED) 1600 else 300
    handler.postDelayed({
        // Step 6: Initialize
        callback.initialize()
        // Step 7: Ready
        callback.onDeviceReady()
    }, delayMs)
}
```

### Connection Parameters

```kotlin
// useAutoConnect() behavior:
connect(device)
    .useAutoConnect(false)  // Direct: connect immediately
                            // Timeout: up to 30 sec
                            // Power: higher consumption
                            // Use: for first-time connection

connect(device)
    .useAutoConnect(true)   // Auto: queues for connection when in range
                            // Timeout: indefinite
                            // Power: lower consumption
                            // Use: for background reconnection
```

---

## GATT Callback Processing

### BluetoothGattCallback Interception

```kotlin
// System's callback
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

// Nordic's wrapper (BleManagerHandler)
class BleManagerHandler : BluetoothGattCallback() {
    private var currentRequest: Request? = null
    private val requestQueue: Deque<Request> = LinkedList()

    override fun onCharacteristicRead(gatt, char, status) {
        if (status == GATT_SUCCESS) {
            // Process value
            valueCallbacks[char.uuid]?.onDataReceived(char.value)
        }

        // Notify current request
        currentRequest?.onCharacteristicRead(gatt, char, status)

        // Continue queue
        currentRequest = null
        processQueue()
    }

    // ... similar for other callbacks
}
```

### Error Code Mapping

```kotlin
// Raw GATT status codes (0x00 to 0xFF) mapped to errors:

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

// Android-specific errors (negative values):
-1     GATT_ERROR (generic)
-2     GATT_CONNECTION_CONGESTED
-3     GATT_SERVICE_NOT_FOUND

// Device disconnects:
133    Connection lost (Nexus 5, 6.0)
134    Connection closed

// Nordic maps to reason codes:
REASON_SUCCESS
REASON_CANCELLED
REASON_TIMEOUT
REASON_DEVICE_DISCONNECTED
REASON_NOT_SUPPORTED
REASON_REQUEST_FAILED
REASON_AUTH_ERROR
```

### State Variables

```kotlin
class BleManagerHandler {
    // Internal state
    var servicesDiscovered = false      // After onServicesDiscovered
    var mtu = 23                        // Default, after onMtuChanged
    var bondState = BOND_NONE           // After bonding check
    var connectionState = STATE_DISCONNECTED
    var lock = Mutex()                  // Protects concurrent access

    // Request tracking
    var currentRequest: Request? = null
    var requestQueue: Deque<Request>
    var initQueue: Deque<Request>

    // Callbacks
    var valueChangedCallback: ValueChangedCallback? = null
    var notificationCallbacks: Map<UUID, ValueChangedCallback>
}
```

---

## Notification Data Pipeline

### Callback Chain

```
Raw BLE Notification
        │
        ▼
┌───────────────────────────────────────┐
│ onCharacteristicChanged()             │
│ (Called by BluetoothGattCallback)     │
└────────┬────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────┐
│ Filter (optional)                      │
│ if (char.uuid == myUuid && ...)       │
└────────┬────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────┐
│ Data Merger (optional)                 │
│ JsonMerger, PacketMerger, etc.        │
│ Reassemble fragmented packets         │
└────────┬────────────────────────────┘
         │
         ▼
┌───────────────────────────────────────┐
│ Final Callback                         │
│ ValueChangedCallback.onDataReceived() │
│ Your app receives complete value      │
└───────────────────────────────────────┘
```

### Callback Registration

```kotlin
// Setup notification callback
val callback: ValueChangedCallback = object : ValueChangedCallback() {
    override fun onDataReceived(device: BluetoothDevice, data: Data) {
        val value = data.getStringValue(0, StandardCharsets.UTF_8)
        Log.d(TAG, "Received: $value")
    }
}

// Register for specific characteristic
setNotificationCallback(characteristic)
    .with(callback)  // Direct callback
    .with(JsonMerger())  // Merge JSON packets first
    .enqueue()
```

### Data Mergers

```kotlin
// No merger (raw data passed through)
setNotificationCallback(statusChar)
    .with(callback)
    .enqueue()

// JSON object merging
setNotificationCallback(jsonDataChar)
    .with(JsonMerger())  // Merges {...} across packets
    .with(callback)
    .enqueue()

// Merge specific JSON field
setNotificationCallback(jsonDataChar)
    .with(JsonMerger("temperature"))  // Only merge this field
    .with(callback)
    .enqueue()

// Custom merger
setNotificationCallback(char)
    .with(object : DataMerger() {
        override fun merge(current: Data, next: Data): Data? {
            // Combine two packets
            val combined = current + next
            // Return null if incomplete, Data if complete
            return if (isComplete(combined)) combined else null
        }
    })
    .with(callback)
    .enqueue()
```

### Blocking Wait for Notification

```kotlin
// Useful for request-response patterns
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

// Kotlin suspending version
val data = manager.waitForNotification(statusChar)
    .timeout(5_000)
    .suspend()

val value = data.getStringValue(0, StandardCharsets.UTF_8)
Log.d(TAG, "Got: $value")
```

---

## Thread Model

### Thread Execution

```kotlin
// Default behavior:
Bluetooth Callback Thread (System BLE thread)
    ├─ BluetoothGattCallback executed here
    ├─ Non-UI thread, limited resources
    ├─ Must not block
    └─ Must not do UI operations

// Nordic's handler:
CallbackHandler (configurable)
    ├─ Default: Main thread
    ├─ Can set to custom Looper
    ├─ Allows UI updates in callbacks
    ├─ Allows processing in background
```

### Configuring Handler

```kotlin
// Constructor
val manager = ProvisioningBleManager(context)

// Default: main thread handler
// Automatically set by Android framework

// Custom handler (background)
val backgroundHandler = Handler(
    Looper.getMainLooper().thread.also {
        // Create custom looper
    }
)

// Set on BleManager (Java API only, not in Kotlin)
// manager.handler = backgroundHandler
```

### Kotlin Extension Behavior

```kotlin
// ble-ktx behavior (important!)
// Sets handler = null

// Effect: Callbacks run on BLE system thread
// Advantages:
// - No main thread blocking
// - Faster callbacks
// Disadvantages:
// - Cannot do UI operations
// - Must be quick

// Your code must handle threading:
manager.statusFlow()  // Returns Flow<Data>
    .flowOn(Dispatchers.Main)  // Switch to main thread
    .collect { data ->
        // Safe to update UI here
    }
```

### Thread Safety

```kotlin
// Request queue is thread-safe
manager.write(char, data)  // Safe from any thread
    .enqueue()

// GATT callbacks are synchronized
class BleManagerHandler {
    private val lock = ReentrantLock()

    override fun onCharacteristicChanged(gatt, char) {
        lock.withLock {
            // Access to shared state protected
        }
    }
}

// But characteristic value read must be in callback
// Don't do:
manager.read(char)
    .done { /* successful */ }
    .enqueue()
val value = char.value  // ❌ Not set yet!

// Do:
manager.read(char)
    .done { device, value ->
        val actualValue = value  // ✓ Set here
    }
    .enqueue()
```

---

## Bonding & Pairing

### Bonding States

```kotlin
// Bonding is persistent pairing + encryption keys stored

BOND_NONE = 10           // Not bonded
BOND_BONDING = 11        // Bonding in progress
BOND_BONDED = 12         // Bonded (pairing stored)

device.bondState         // Read current state
device.createBond()      // Initiate bonding
device.removeBond()      // Clear stored bonding
```

### BLE Pairing Methods

```kotlin
// 1. Just Works (no user interaction)
// - No PIN required
// - Most common for IoT
// - Vulnerable to MITM attacks
// → Acceptable for provisioning if BLE range is short

// 2. Passkey Entry (user types PIN)
// - More secure
// - User sees 6-digit PIN on both devices
// - User enters PIN on device without display

// 3. Numeric Comparison (accept/reject)
// - Show 6-digit number on both devices
// - User confirms numbers match
// - Fastest secure pairing

// 4. Out-of-Band (NFC, QR, etc.)
// - Highest security
// - Pre-shared secret via secondary channel
// - Requires NFC or QR scanning

// 5. LE Secure Connections
// - Uses ECDH key exchange
// - Resists MITM attacks
// - Supported on BLE 4.2+
```

### Requesting Bonding

```kotlin
// 1. Automatic bonding request
val bondReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
            val device = intent.getParcelableExtra<BluetoothDevice>(
                BluetoothDevice.EXTRA_DEVICE
            )

            // Accept pairing
            device?.setPairingConfirmation(true)
        }
    }
}

val filter = IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST)
registerReceiver(bondReceiver, filter)

// 2. Initiate bonding (Nordic)
manager.ensureBond()
    .done { /* Success */ }
    .fail { /* Failure */ }
    .enqueue()
```

### Bonding in Provisioning Flow

```kotlin
connect(device)
    .retry(3, 200)
    .timeout(15_000)
    .done { /* Connected */ }
    .enqueue()

ensureBond()
    .done { /* Bonded */ }
    .enqueue()

requestMtu(512)
    .done { /* MTU negotiated */ }
    .enqueue()

// All execute sequentially
```

---

## MTU & Data Fragmentation

### MTU (Maximum Transmission Unit)

```
L2CAP/ATT MTU

Default: 23 bytes
Maximum: 517 bytes (practical: 512)

Breakdown for Write operation:
┌────────────────────────────────┐
│  23-byte default MTU           │
├────────────────────────────────┤
│ Opcode (1) | Handle (2) | ... │ Header: 3 bytes
├────────────────────────────────┤
│ Payload Data              | CRC│ Data: 20 bytes
└────────────────────────────────┘

With MTU 512:
┌────────────────────────────────┐
│  512-byte negotiated MTU       │
├────────────────────────────────┤
│ Header (3 bytes)               │
├────────────────────────────────┤
│ Payload Data (509 bytes)       │
└────────────────────────────────┘
```

### MTU Negotiation

```kotlin
// Request larger MTU
manager.requestMtu(512)
    .done { mtu ->
        Log.d(TAG, "MTU set to: $mtu")  // May be less than requested
    }
    .fail { device, status ->
        Log.e(TAG, "MTU request failed: $status")
    }
    .enqueue()

// Result:
// - Success: onMtuChanged called
// - Failure: Stays at default 23 bytes
// - Don't fail if MTU stays at 23, some devices don't support
```

### Data Segmentation

```kotlin
// Writing large data (> MTU - 3):
manager.write(largeDataChar, Data(1000 bytes))
    .enqueue()

// Internally: Auto-segments
// MTU 512:
// Segment 1: 509 bytes
// Segment 2: 491 bytes
// Waits for ACK after each segment

// No manual intervention needed
// Library handles segmentation transparently
```

### Reliable Write (Deprecated Pattern)

```kotlin
// Old style: Reliable Write with echo
ReliableWriteRequest()
    .write(char1, data1)  // Send, get echo
    .write(char2, data2)  // Send, get echo
    .done { /* All wrote successfully */ }
    .enqueue()

// Not recommended:
// - Slower (echo round-trip)
// - Only for devices with buggy firmware
// - Use normal Write for new designs
```

---

## Kotlin Extensions (ble-ktx)

### Suspend Functions

```kotlin
// Traditional callback style
manager.write(char, data)
    .done { Log.d(TAG, "Wrote") }
    .fail { _, reason -> Log.e(TAG, "Failed: $reason") }
    .enqueue()

// Kotlin suspend style
try {
    manager.write(char, data).suspend()
    Log.d(TAG, "Wrote successfully")
} catch (e: Exception) {
    Log.e(TAG, "Failed: ${e.message}")
}
```

### Flow Integration

```kotlin
// Read as Flow (hot stream)
manager.statusChar.asFlow(manager)
    .collect { data ->
        val value = data.getStringValue(0)
        Log.d(TAG, "Status: $value")
    }

// State as Flow
manager.stateAsFlow()  // BleManager.STATE_*
    .collect { state ->
        when (state) {
            BleManager.STATE_CONNECTED -> { /* ... */ }
            BleManager.STATE_DISCONNECTED -> { /* ... */ }
        }
    }

// Bonding state as Flow
manager.bondingStateAsFlow()
    .collect { bondState ->
        when (bondState) {
            BluetoothDevice.BOND_BONDED -> { /* ... */ }
        }
    }

// Request-response Flow
manager.readBatteryLevel()
    .asResponseFlow<Int>()
    .collect { level ->
        Log.d(TAG, "Battery: $level%")
    }
```

### Error Mapping

```kotlin
// BLE errors mapped to Kotlin exceptions:

REASON_SUCCESS
    → Completes successfully

REASON_CANCELLED
    → Throws CancellationException

REASON_TIMEOUT
    → Throws TimeoutException

REASON_DEVICE_DISCONNECTED
    → Throws DeviceDisconnectedException

REASON_NOT_SUPPORTED
    → Throws RequestNotSupportedException

REASON_REQUEST_FAILED
    → Throws RequestFailedException

// Usage:
try {
    manager.enableNotifications(char).suspend()
} catch (e: TimeoutException) {
    Log.e(TAG, "Notification enable timed out")
} catch (e: CancellationException) {
    Log.i(TAG, "Notification enable cancelled by user")
}
```

### Flow Operators

```kotlin
// Built-in helpers
manager.writeAndWait(char, data)  // Write then wait for notification

manager.readAndWait(char)         // Read, then subscribe notifications

manager.asValidResponseFlow<String>()  // Emits non-empty responses only

// With transformation
manager.stateAsFlow()
    .map { state -> stateToString(state) }
    .distinctUntilChanged()
    .collect { statusStr ->
        updateUI(statusStr)
    }

// With filtering
manager.statusFlow()
    .filter { it.contains("SUCCESS") }
    .take(1)
    .collect { /* First success only */ }
```

### Scope & Lifecycle

```kotlin
// Coroutine scope binding
lifecycleScope.launch {
    try {
        manager.write(char, data).suspend()
    } catch (e: Exception) {
        // Automatically cancelled if activity destroyed
    }
}

// Flow collection
viewModelScope.launch {
    manager.stateAsFlow()
        .collect { state ->
            // Cancelled with viewModel scope
        }
}
```

---

## Server-Side Implementation

### BleServerManager

```kotlin
// Create server
val serverManager = BleServerManager(context)

// Define service with DSL
serverManager.server(serviceUuid)
    .service {
        characteristic(charUuid, properties, permissions) {
            onWrite { device, value ->
                Log.d(TAG, "Received: ${String(value)}")
            }
        }
        characteristic(statusUuid, PROPERTY_READ or PROPERTY_NOTIFY, ...) {
            onRead { device ->
                // Return current value
                deviceInfo.toString().toByteArray()
            }
        }
        descriptor(cccdUuid, ...)  // Auto-added for NOTIFY/INDICATE
    }

// Start server
serverManager.open()
    .done { /* Listening */ }
    .fail { reason -> /* Failed */ }
    .enqueue()

// Send notifications
serverManager.sendNotification(device, char, data)
    .done { /* Sent */ }
    .enqueue()

// Close server
serverManager.close()
```

### Per-Client Shared Characteristics

```kotlin
// Shared (default)
characteristic(uuid, PROPERTY_NOTIFY, ...)
    // All connected clients receive same notifications

// Per-client
sharedCharacteristic(uuid, PROPERTY_NOTIFY, ...)
    // Each client gets separate callback

// Example:
sharedCharacteristic(statusUuid, ...) {
    onNotified { device ->
        // Called when client disables notifications
        Log.d(TAG, "Client ${device.address} disabled notifications")
    }
}
```

### Waiting for Client Actions

```kotlin
// Wait for client write
WaitForWriteRequest(char)
    .done { device, data ->
        val value = String(data)
        Log.d(TAG, "Client wrote: $value")
    }
    .timeout(30_000)
    .enqueue()

// Wait for read
WaitForReadRequest(char)
    .done { device ->
        Log.d(TAG, "Client reading...")
        // Return data via onRead callback
    }
    .enqueue()
```

---

## Best Practices

### Error Recovery

```kotlin
// Always retry operations
write(char, data)
    .retry(2, 100)  // 2 retries, 100ms between
    .timeout(5_000)
    .fail { device, reason ->
        when (reason) {
            REASON_TIMEOUT -> { /* Retry logic */ }
            REASON_DEVICE_DISCONNECTED -> { /* Reconnect */ }
        }
    }
    .enqueue()
```

### Resource Cleanup

```kotlin
override fun onCleared() {
    // ViewModel cleanup
    manager.disconnect().enqueue()
    manager.close()
}

override fun onDestroy() {
    // Activity cleanup
    unregisterReceiver(bondReceiver)
    super.onDestroy()
}
```

### Timeout Strategy

```kotlin
// Connection: Aggressive (want to know quickly)
connect(device).timeout(15_000)

// Write: Moderate (BLE is slower than BT)
write(char, data).timeout(5_000)

// Notification wait: Long (could be user-triggered)
waitForNotification().timeout(30_000)

// Scan: Very long (user might not be near)
startScan(10_000 or more)
```

### Testing

```kotlin
// Use in-memory mock
class MockBleManager : BleManager() {
    override fun getGattCallback() = object : BleManagerGattCallback() {
        override fun isRequiredServiceSupported(gatt) = true
        override fun initialize() { /* */ }
    }

    fun simulateNotification(data: Data) {
        // Trigger callback directly
        valueChangedCallback?.onDataReceived(mockDevice, data)
    }
}

// Unit test
@Test
fun testProvisioning() {
    val manager = MockBleManager()
    // Simulate writes, notifications
    // Assert state changes
}
```

---

## See Also

- [Project README](README.md) - Overview
- [Technical Analysis](ANALYSIS.md) - BLE concepts
- [Technology Ecosystem](BLE_TECHNOLOGY_ECOSYSTEM.md) - Related standards

## References

- [Nordic BLE Library GitHub](https://github.com/NordicSemiconductor/Android-BLE-Library)
- [Android BluetoothGatt Documentation](https://developer.android.com/reference/android/bluetooth/BluetoothGatt)
- [BLE Specification (SIG)](https://www.bluetooth.com/specifications/specs/)
