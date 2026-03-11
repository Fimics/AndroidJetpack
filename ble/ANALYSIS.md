# BLE WiFi Provisioning - Technical Analysis

## Table of Contents
1. [BLE Foundation Concepts](#ble-foundation-concepts)
2. [WiFi Provisioning Methods Comparison](#wifi-provisioning-methods-comparison)
3. [GATT Protocol Design](#gatt-protocol-design)
4. [Android BLE Permissions Evolution](#android-ble-permissions-evolution)
5. [Technology Stack Rationale](#technology-stack-rationale)
6. [Data Flow & Timing](#data-flow--timing)
7. [Security Architecture](#security-architecture)

---

## BLE Foundation Concepts

### Protocol Stack Layers

BLE operates across multiple protocol layers, each with specific responsibilities:

```
┌─────────────────────────────────────────────┐
│            Application Layer                 │
│  (Your App: BleClientActivity, ViewModel)   │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│        GATT (Generic Attribute Profile)      │
│  Services, Characteristics, Descriptors     │
│  Read, Write, Notify, Indicate operations  │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│   ATT (Attribute Protocol)                   │
│  Request/Response, Handle-based addressing  │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│  L2CAP (Logical Link Control & Adaptation)  │
│  Segmentation, Reassembly, Multiplexing    │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│    Link Layer (Bluetooth LE)                 │
│  Advertising, Scanning, Connection State   │
└────────────────┬────────────────────────────┘
                 │
┌────────────────▼────────────────────────────┐
│    Physical Layer (RF)                       │
│  2.4 GHz ISM Band, 40 channels              │
└─────────────────────────────────────────────┘
```

### GAP (Generic Access Profile)
Defines roles and procedures for device discovery and connection:

| Concept | Definition |
|---------|-----------|
| **Broadcaster** | Sends advertising packets (no connection) |
| **Observer** | Listens for advertising packets |
| **Central** | Initiates connections (can connect to multiple devices) |
| **Peripheral** | Accepts connections (typically single connection) |

Our implementation:
- **ble-client** → Central role (scans and initiates connections)
- **ble-server** → Peripheral role (advertises and accepts connections)

### GATT (Generic Attribute Profile)
Defines hierarchical attribute structure:

```
Service (UUID)
  ├─ Characteristic (UUID)
  │   ├─ Property (READ, WRITE, NOTIFY, INDICATE, etc.)
  │   ├─ Descriptor (CCCD, etc.)
  │   └─ Value (0-512 bytes in BLE 5.x)
  ├─ Characteristic
  └─ ...

Service
  ├─ Characteristic
  └─ ...
```

### Service, Characteristic, Descriptor Structure

```
Provisioning Service (12345678-1234-1234-1234-1234567890AB)
│
├─ SSID Characteristic (12345678-1234-1234-1234-1234567890AC)
│   └─ Property: WRITE
│
├─ Password Characteristic (12345678-1234-1234-1234-1234567890AD)
│   └─ Property: WRITE
│
└─ Status Characteristic (12345678-1234-1234-1234-1234567890AE)
    ├─ Property: READ, NOTIFY
    └─ Client Characteristic Configuration Descriptor (CCCD)
        └─ UUID: 00002902-0000-1000-8000-00805f9b34fb
        └─ Allows client to enable/disable notifications
```

### Characteristic Properties

| Property | Direction | Response | Use Case |
|----------|-----------|----------|----------|
| **READ** | Server→Client | Required | Get current value |
| **WRITE** | Client→Server | Required | Send small data |
| **WRITE_NO_RESPONSE** | Client→Server | None | Fast writes (no ack) |
| **NOTIFY** | Server→Client | None | Async status updates |
| **INDICATE** | Server→Client | Required | Reliable async updates |
| **SIGNED_WRITE** | Client→Server | None | Encrypted without pairing |

Our design:
- SSID/Password: WRITE (client sends, no response needed)
- Status: READ + NOTIFY (client subscribes for updates)

---

## WiFi Provisioning Methods Comparison

### Overview Table

| Method | Technology | Pros | Cons | Range | Speed |
|--------|-----------|------|------|-------|-------|
| **BLE Provisioning** (this project) | Bluetooth LE | No WiFi switch, low power, secure pairing option | Requires BLE hardware | 10-30m | 1-5s |
| **SoftAP** | WiFi AP | Straightforward, no Bluetooth needed | User must switch WiFi network | 10-30m | 5-10s |
| **SmartConfig (ESP-Touch)** | UDP broadcast | Invisible to user, no switching | Poor wall penetration, unreliable | 5-20m | 10-30s |
| **ZeroConf / mDNS** | UDP multicast | Auto-discovery, simple | Same network required | LAN only | 2-5s |
| **BLE Mesh Provisioning** | Bluetooth Mesh | One-to-many provisioning | Complex protocol overhead | 10-30m | 5-20s |
| **NFC** | NFC/RFID | Tap-based, no scanning | Very short range, special hardware | 5-10cm | 1-2s |
| **QR Code** | Visual | Fast, offline capable | Still requires manual steps | - | Variable |

### Detailed Comparison

#### BLE Provisioning (Our Implementation)
```
Advantages:
✓ Non-intrusive - phone stays on original WiFi
✓ Low power consumption (BLE << WiFi)
✓ Standard protocol (GATT)
✓ Good security model (pairing, bonding, encryption)
✓ Wide device support
✓ Works in offline/airplane mode (after WiFi handoff)

Disadvantages:
✗ Requires Bluetooth on both devices
✗ Requires Android 7.0+ (BLE standard, API 24+)
✗ Cannot provision WiFi-only devices (headless)
✗ Limited credential data size (MTU ~512 bytes)
✗ Android 12+ requires location permissions
```

#### SoftAP
```
Advantages:
✓ Simple implementation
✓ No special hardware beyond WiFi
✓ Fast connection

Disadvantages:
✗ User must manually switch WiFi network
✗ User must switch back after provisioning
✗ Network name/password visible
✗ Interference with device's normal operation
✗ Bad UX for consumer products
```

#### SmartConfig (ESP-Touch, Espressif)
```
Advantages:
✓ Transparent to user
✓ User stays on current WiFi
✓ Simple implementation

Disadvantages:
✗ Not standardized (vendor-specific)
✗ Unreliable in crowded 2.4GHz bands
✗ Cannot transfer large data
✗ Requires ESP32/ESP8266 hardware
✗ Patent/licensing considerations
```

#### ZeroConf/mDNS
```
Advantages:
✓ Automatic discovery
✓ No pre-shared password needed

Disadvantages:
✗ Requires same network segment
✗ Not suitable for initial provisioning
✗ Multicast not reliable over WiFi
✗ Cannot work without network connection
```

#### BLE Mesh Provisioning (SIG Standard)
```
Advantages:
✓ One provisioning device to many nodes
✓ Standard protocol (SIG Mesh)
✓ Excellent security model

Disadvantages:
✗ Complex protocol (not lightweight)
✗ Overkill for simple provisioning
✗ Requires Mesh-aware hardware
✗ Longer development cycle
```

### Decision Matrix for BLE Provisioning

Choose BLE provisioning when:
- ✅ Device has Bluetooth capability
- ✅ User has smartphone (phone as provisioning tool)
- ✅ Non-intrusive UX is critical
- ✅ Power consumption matters (IoT devices)
- ✅ Security/encryption is required
- ✅ Works offline after initial provisioning

Do NOT choose BLE when:
- ❌ Target device has no Bluetooth
- ❌ User has feature phone only
- ❌ High-speed bulk data transfer needed
- ❌ Must work in BLE-jammed environments
- ❌ Latency must be < 100ms

---

## GATT Protocol Design

### Service Design

#### Custom vs Standard UUIDs

**Custom UUIDs (This Project):**
```
Format: xxxxxxxx-1234-1234-1234-xxxxxxxxxxxx
Example: 12345678-1234-1234-1234-1234567890AB

Advantages:
✓ Unique per company/product
✓ Full control over service design
✓ No SIG registry required

Disadvantages:
✗ Not recognized by generic tools
✗ Must document protocol
```

**Standard SIG UUIDs:**
```
Format: 0000xxxx-0000-1000-8000-00805f9b34fb
Example: 180A = Device Information Service (SIG)

Advantages:
✓ Well-known by tools/libraries
✓ Interoperable
✓ Less documentation needed

Disadvantages:
✗ Must conform to spec
✗ Cannot customize
```

For WiFi provisioning, custom UUIDs are appropriate since no standard exists for this use case.

### Characteristic Design Decisions

#### SSID and Password: WRITE Property

```
Reasoning:
- Client needs to WRITE credentials to server
- WRITE property = bidirectional flow
  - Client sends: value to write
  - Server responds: success/failure status
- WRITE_NO_RESPONSE would be faster but riskier
  - No confirmation from server
  - If write fails, client doesn't know

Data model:
- SSID: UTF-8 string, max 32 bytes (WiFi standard)
- Password: UTF-8 string, max 63 bytes (WiFi standard)
- Both use WRITE property for reliability
```

#### Status: READ + NOTIFY

```
Reasoning:
- Server needs to send status asynchronously
- READ: Client can query current status immediately
- NOTIFY: Server pushes status changes to client
  - Avoids constant polling
  - Client subscribes via CCCD (Client Characteristic
    Configuration Descriptor)
  - Server doesn't queue notifications if client disconnected

Status values:
- IDLE: Waiting for credentials
- RECEIVING_CREDENTIALS: Got SSID/Password
- CONNECTING_TO_WIFI: Attempting connection
- SUCCESS: Connected to WiFi
- FAILED: Connection failed
```

### Descriptor: Client Characteristic Configuration Descriptor (CCCD)

```
UUID: 00002902-0000-1000-8000-00805f9b34fb
Standard SIG descriptor for all NOTIFY/INDICATE characteristics

Value:
- 0x0000 = Notifications disabled
- 0x0001 = Notifications enabled
- 0x0002 = Indications enabled
- 0x0003 = Both enabled

Automatically added by GATT server implementation
Allows client to subscribe/unsubscribe from notifications
```

---

## Android BLE Permissions Evolution

### API 23 (Android 6.0) - Original Coarse Model

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

<!-- Runtime permission request -->
if (ContextCompat.checkSelfPermission(context, BLUETOOTH)
    != PERMISSION_GRANTED) {
    ActivityCompat.requestPermissions(...)
}
```

### API 29 (Android 10) - Finer Granularity

```xml
<!-- API 29 introduced:-->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- Still required for BLE scanning -->
```

### API 31 (Android 12) - Major Reorganization

```xml
<!-- BLUETOOTH split into specific operations -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- Still need legacy for API 30- -->
<uses-permission android:name="android.permission.BLUETOOTH" android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" android:maxSdkVersion="30" />

<!-- Location still required for scanning -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

### Android 12+ Runtime Permission Request

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

### Our Implementation

**AndroidManifest.xml (ble-client):**
```xml
<!-- Covers API 23-30 and 31+ -->
<uses-permission android:name="android.permission.BLUETOOTH"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN"
    android:maxSdkVersion="30" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

**BleClientActivity.kt (Runtime Requests):**
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

### Key Permission Classes

| Permission | API | Purpose | Requirement |
|-----------|-----|---------|-------------|
| BLUETOOTH | 4+ | Legacy BLE access | Removed in API 31+ |
| BLUETOOTH_ADMIN | 4+ | Legacy admin access | Removed in API 31+ |
| BLUETOOTH_SCAN | 31+ | BLE scanning | Runtime on API 31+ |
| BLUETOOTH_CONNECT | 31+ | BLE connections | Runtime on API 31+ |
| BLUETOOTH_ADVERTISE | 31+ | BLE advertising | Runtime on API 31+ |
| ACCESS_FINE_LOCATION | 6+ | WiFi/BLE location info | Runtime on API 6+ |
| ACCESS_COARSE_LOCATION | 6+ | Network location | Alternative to FINE |

---

## Technology Stack Rationale

### Why Nordic BLE Library?

#### Comparison with Alternatives

| Aspect | Nordic BLE | Android Native BluetoothGatt | RxAndroidBle |
|--------|-----------|------|------------|
| **Learning Curve** | Medium | Steep | Medium-High |
| **Reliability** | Very High | Buggy/Fragmented | Very High |
| **Active Maintenance** | Yes (2024+) | Built-in | Community |
| **Request Queue** | Built-in (serial) | Manual sync | Built-in |
| **Error Handling** | Mapped errors | Raw codes | Good |
| **Documentation** | Good | Sparse | Fair |
| **Kotlin Support** | KTX extension | Limited | Limited |
| **Sample Projects** | Many | Few | Some |
| **Callback Hell** | Minimized | Callback chains | Rx operators |

#### Nordic BLE 2.11.0 Features

```kotlin
// Request queue (critical for stability)
manager.requestMtu(512)
    .retry(1, 100)
    .done { status -> Log.d("MTU negotiated") }
    .enqueue()

// Automatic state management
connect(device)
    .retry(3, 200)
    .timeout(15_000)
    .useAutoConnect(false)
    .enqueue()

// Error mapping
.fail { device, status ->
    when (status) {
        BinderError -> // System error
        GattError -> // GATT protocol error
        RequestError -> // Request validation error
    }
}

// Kotlin Coroutines support (ble-ktx)
val mtu = manager.requestMtu(512).suspend()
manager.getStatusFlow().collect { status ->
    // Reactive updates
}
```

### Why Kotlin Coroutines + Flow over RxJava?

| Feature | Coroutines + Flow | RxJava 3 |
|---------|-------------------|----------|
| **Learning Curve** | Easier (sequential code) | Steeper (operator chains) |
| **Memory Overhead** | Lower | Higher (Observable objects) |
| **Error Handling** | Try-catch | operators (catching, errorHandler) |
| **Cancellation** | Built-in (suspend) | Manual (Disposable) |
| **Standard (Google)** | Official (Kotlin stdlib) | 3rd-party |
| **Android Jetpack** | Full integration | Partial (Rx adapters) |
| **Testing** | `runTest { }` | TestScheduler |
| **Hot Streams** | SharedFlow | BehaviorSubject, PublishSubject |
| **Backpressure** | Less relevant (UI) | Handled with operators |

Our choice:
- Coroutines for BLE operations (sequential, cleaner code)
- Flow for reactive UI updates (better than LiveData)
- StateFlow for MVVM state management

### Why MVVM Architecture?

```
Model Layer:
- WiFiCredentials data class
- ProvisioningStatus enum

ViewModel Layer:
- BleClientViewModel
- BleServerViewModel
- Manages BLE state (scanned devices, connection, provisioning)
- Outlives Activity rotation
- Provides StateFlow for UI subscription

View Layer:
- BleClientActivity / BleServerActivity
- RecyclerView for device list
- Observes ViewModel.StateFlow
- Handles permission requests
- Triggers ViewModel actions
```

Benefits:
✓ Separation of concerns
✓ Testable business logic (ViewModel)
✓ Lifecycle-aware state
✓ Survives configuration changes
✓ Easy to test with unit tests

---

## Data Flow & Timing

### Connection Flow Sequence

```
Client                              Server
 │                                   │
 ├─ Scan (10 sec timeout)           │ Advertises every 100-200ms
 │                                   │
 ├─ Receives scan result            │
 │                                   │
 ├─ Initiates connection ───────→   ├─ Accepts connection
 │ (up to 30 sec timeout)           │
 │                                   │
 ├─ Discovers GATT services ─────→  ├─ No action needed
 │ (1-5 sec, device cached)         │
 │                                   │
 ├─ Negotiates MTU (23→512) ────→   ├─ Responds with MTU
 │ (default 512 bytes)              │
 │                                   │
 ├─ Enables Status notifications ──→ ├─ Records notification enabled
 │ (writes to CCCD)                 │
 │                                   │
 └─ Ready for provisioning          └─ Ready to serve
```

### Provisioning Data Exchange Timing

```
Timeline:
T+0ms    | Client                        | Server
T+0      | Write SSID                   →|
T+50     |← GATT write response         |
T+100    | Write Password               →|
T+150    |← GATT write response         |
T+200    | Notification enabled         | Status = RECEIVING_CREDENTIALS
T+300    |← Notification: RECEIVING... →|
T+500    |                              | Attempt WiFi connection
T+1000   |← Notification: CONNECTING... →|
T+2000   |                              | WiFi connected ✓
T+2100   |← Notification: SUCCESS       →|
T+2200   | Connection success! Disconnect→|
         |                              | Stop advertising
```

### Timeout Strategy

```kotlin
// Connection attempt
connect(device)
    .timeout(15_000)     // 15 second max
    .retry(3, 200)       // 3 retries, 200ms between
    .enqueue()

// SSID write
writeSSID(ssid)
    .timeout(5_000)      // 5 second max
    .retry(1, 100)       // 1 retry, 100ms between
    .enqueue()

// Status subscription
statusFlow.collect { status ->
    // Indefinite wait (user can manually disconnect)
}
```

---

## Security Architecture

### Current Implementation

**Status**: No encryption/authentication (proof-of-concept)

```
Device A (Client)
└─ SSID: "MyNetwork" ──BLE→ Device B (Server)
└─ Password: "pass123" ──BLE→
```

**Risk**: If BLE device is eavesdropped on, credentials exposed.

### Recommended Security Enhancements

#### 1. BLE Pairing

```kotlin
// Enable pairing
device.createBond()

// Pairing variants:
// - Just Works: No out-of-band confirmation
// - Passkey Entry: User enters PIN on both devices
// - Numeric Comparison: Display numbers, user confirms same
// - Out-of-Band: Pre-shared secret via NFC/QR
```

#### 2. AES Encryption

```kotlin
// Pre-shared key (PSK) encryption
private val psk = byteArrayOf(...)  // 16-32 bytes

fun encryptCredentials(ssid: String, password: String): ByteArray {
    val plaintext = "$ssid|$password".toByteArray()
    val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    val secretKey = SecretKeySpec(psk, 0, psk.size, "AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    return cipher.doFinal(plaintext)
}

// Write encrypted blob
writeCharacteristic(GattUUID.CREDENTIALS, encryptedBytes)
    .enqueue()
```

#### 3. HMAC Signing

```kotlin
// Verify message integrity
fun signMessage(message: ByteArray, secret: ByteArray): ByteArray {
    val hmac = Mac.getInstance("HmacSHA256")
    hmac.init(SecretKeySpec(secret, "HmacSHA256"))
    return hmac.doFinal(message)
}

// Characteristic: [message_length (2 bytes)][message][hmac (32 bytes)]
val signature = signMessage(credentials, psk)
val payload = credentials + signature
writeCharacteristic(GattUUID.SIGNED_CREDENTIALS, payload)
    .enqueue()
```

#### 4. Challenge-Response

```kotlin
// Server sends random challenge to client
val challenge = Random().nextBytes(16)

// Client computes response
val response = HMAC-SHA256(psk, challenge + credentials)

// Server verifies
if (received_response == expected_response) {
    // Authenticate and proceed
}
```

#### 5. Time-Bound Tokens

```kotlin
// Server generates provisioning token (valid 5 minutes)
val token = generateToken()
val expiresAt = System.currentTimeMillis() + 5 * 60_1000

// Client must send token with credentials
writeCharacteristic(GattUUID.CREDENTIALS,
    token + expiresAt + credentials)
    .enqueue()

// Server validates token before accepting
if (currentTime < expiresAt && validateToken(token)) {
    acceptProvisioning()
}
```

### Production Security Checklist

- [ ] Enable BLE pairing (LE Secure Connections, SMP)
- [ ] Use AES-128 minimum encryption for credential payload
- [ ] Implement HMAC-SHA256 message authentication
- [ ] Use WPA3 for WiFi credentials (not WPA2)
- [ ] Implement rate limiting (max 5 provisioning attempts/minute)
- [ ] Add timeout to provisioning mode (auto-disable after 10 min)
- [ ] Implement device attestation (certificate pinning)
- [ ] Use HTTPS for any cloud communication
- [ ] Implement secure boot on embedded device
- [ ] Enable WiFi scanning encryption (MAC randomization)
- [ ] Log provisioning attempts for audit trail
- [ ] Document security posture in user manual

---

## See Also

- [Project README](README.md) - Project overview and quick start
- [Nordic BLE Deep Dive](NORDIC_BLE_DEEP_DIVE.md) - Library internals
- [BLE Technology Ecosystem](BLE_TECHNOLOGY_ECOSYSTEM.md) - Related standards
