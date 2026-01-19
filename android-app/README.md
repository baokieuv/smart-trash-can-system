# 📱 Smart Bin Android App

Mobile application để quản lý và giám sát hệ thống Smart Bin IoT.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ)
- [Yêu cầu](#-yêu-cầu)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Cấu trúc code](#-cấu-trúc-code)
- [Architecture](#-architecture)
- [API Integration](#-api-integration)
- [Bluetooth Integration](#-bluetooth-integration)
- [Screenshots](#-screenshots)
- [Build & Release](#-build--release)

---

## 🎯 Giới thiệu

Smart Bin Android App là ứng dụng di động cho phép người dùng:

- Quản lý thiết bị Smart Bin
- Giám sát trạng thái thiết bị real-time
- Thêm thiết bị mới qua Bluetooth
- Cấu hình WiFi cho ESP32-CAM
- Xem thống kê và lịch sử phân loại rác
- Nhận thông báo khi thùng rác đầy

---

## ✨ Tính năng

### 🔐 Authentication
- ✅ Đăng ký tài khoản với email verification
- ✅ Đăng nhập với Keycloak OAuth2
- ✅ Refresh token tự động
- ✅ Đổi mật khẩu
- ✅ Đăng xuất

### 🎛️ Quản lý thiết bị
- ✅ Xem danh sách thiết bị
- ✅ Thêm thiết bị mới qua Bluetooth
- ✅ Cấu hình WiFi cho ESP32-CAM
- ✅ Đổi tên thiết bị
- ✅ Xóa thiết bị
- ✅ Xem chi tiết thiết bị (fill level, battery, waste stats)

### 📊 Dashboard & Thống kê
- ✅ Tổng số thiết bị
- ✅ Thiết bị online/offline
- ✅ Phân loại rác theo loại (recyclable, organic, non-recyclable)
- ✅ Mức độ đầy của thùng (fill level)
- ✅ Auto-refresh mỗi 30 giây

### 🔔 Thông báo
- ✅ Danh sách notification
- ✅ Phân loại theo type (SUCCESS, WARNING, ERROR, INFO)
- ✅ Click notification để xem chi tiết device

### 🎨 UI/UX
- ✅ Material Design 3
- ✅ Dark/Light mode
- ✅ Responsive layouts
- ✅ Pull-to-refresh
- ✅ Smooth animations

---

## 🚀 Công nghệ

| Technology | Version | Purpose |
|------------|---------|---------|
| **Android SDK** | API 24-36 (Nougat 7.0 - Android 15) | Platform |
| **Java** | 11 | Programming language |
| **Material Design** | 3.x | UI components |
| **ViewBinding** | Latest | View access |
| **DataBinding** | Latest | Data binding |
| **Gson** | 2.10.1 | JSON parsing |
| **RecyclerView** | Latest | List rendering |
| **SwipeRefreshLayout** | Latest | Pull to refresh |
| **Glide** | 4.16.0 | Image loading |
| **Bluetooth Classic** | - | Device pairing |

---

## 📱 Yêu cầu

### Development Environment
- **Android Studio**: Ladybug | 2024.2.1 or newer
- **JDK**: 11 or higher
- **Gradle**: 8.x (auto-installed by wrapper)
- **Min SDK**: 24 (Android 7.0 Nougat)
- **Target SDK**: 36 (Android 15)
- **Compile SDK**: 36

### Device/Emulator Requirements
- Android 7.0 (Nougat) or higher
- Bluetooth support (for device pairing)
- Internet connection
- Camera permission (optional, for future features)

---

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/android-app
```

### 2. Mở project trong Android Studio

- File → Open
- Chọn thư mục `android-app`
- Sync Gradle

### 3. Cấu hình API endpoints

File: `app/src/main/java/com/example/smart_bin/utils/Constants.java`

```java
public class Constants {
    // Server URLs - THAY ĐỔI THEO SERVER CỦA BẠN
    public static final String BASE_URL = "http://YOUR_SERVER_IP:8080";
    public static final String API_VERSION = "/api/v1";
    
    // Endpoints
    public static final String DEVICES_ENDPOINT = BASE_URL + API_VERSION + "/devices";
    // ...
}
```

**⚠️ Lưu ý**: 
- Không dùng `localhost` hoặc `127.0.0.1`
- Dùng IP thực của server (VD: `192.168.1.100`)
- Hoặc domain name nếu deploy lên internet

### 4. Cấu hình permissions

File: `app/src/main/AndroidManifest.xml`

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.CHANGE_WIFI_STATE" />

<!-- Bluetooth permissions -->
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" 
    android:usesPermissionFlags="neverForLocation" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

<!-- Location (required for Bluetooth scanning on Android 6+) -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### 5. Build project

```bash
./gradlew assembleDebug
```

Hoặc trong Android Studio: **Build → Make Project**

---

## 📂 Cấu trúc code

```
app/src/main/java/com/example/smart_bin/
│
├── api/                        # API Services
│   ├── ApiService.java        # Device & data API calls
│   └── AuthService.java       # Authentication API calls
│
├── bluetooth/                  # Bluetooth
│   └── BLEManager.java        # Bluetooth LE connection
│
├── wifi/                       # WiFi
│   └── WiFiScanner.java       # WiFi scanning
│
├── model/                      # Data Models
│   ├── User.java
│   ├── Device.java
│   ├── DeviceData.java
│   ├── Notification.java
│   └── AuthResponse.java
│
├── adapter/                    # RecyclerView Adapters
│   ├── DeviceAdapter.java
│   └── NotificationAdapter.java
│
├── fragments/                  # UI Fragments
│   ├── HomeFragment.java      # Device list
│   ├── NotificationsFragment.java
│   └── SettingsFragment.java
│
├── utils/                      # Utilities
│   ├── Constants.java         # App constants
│   ├── TokenManager.java      # Token storage & refresh
│   └── NetworkUtils.java      # Network helpers
│
├── LoginActivity.java          # Login screen
├── RegisterActivity.java       # Register screen
├── MainActivity.java           # Main container
├── DeviceControlActivity.java  # Device detail screen
├── AddDeviceActivity.java      # Add device via BLE
├── WiFiReconfigureActivity.java # WiFi config
└── ChangePasswordActivity.java # Change password

app/src/main/res/
├── layout/                     # XML Layouts
├── drawable/                   # Images & icons
├── values/
│   ├── strings.xml
│   ├── colors.xml
│   └── themes.xml
└── menu/
    └── bottom_menu.xml         # Bottom navigation
```

---

## 🏗️ Architecture

**Pattern**: MVC + Service Layer

### Layers:
- **Activities/Fragments**: UI và user interactions
- **Service Layer** (tách riêng theo domain):
  - `AuthService`: Authentication APIs (login, register, refresh token)
  - `DeviceService`: Device CRUD operations
  - `DeviceDataService`: Device data/statistics
  - `NotificationService`: Notification management
- **Models**: Data objects (Device, DeviceData, Notification, User)
- **Utils**: TokenManager, Constants, NetworkUtils
- **Bluetooth**: BLEManager (Bluetooth Classic connection)
- **WiFi**: WiFiScanner (WiFi network scanning)

### Token Management:
- Login → Lưu access/refresh token vào SharedPreferences
- Mỗi API call → Auto check expiry → Refresh nếu cần
- Refresh fail → Navigate về LoginActivity
- Mỗi Service có built-in token refresh logic

---

## 📡 API Integration

API Layer được tách thành các Service riêng biệt, mỗi Service xử lý một domain cụ thể.

### DeviceService.java
**Package**: `com.example.smart_bin.api`

**Chức năng**:
- `fetchDevices(DevicesCallback)`: Lấy danh sách devices
- `createDevice(Device, DeviceCallback)`: Tạo device mới
- `getDevice(String deviceId, DeviceCallback)`: Lấy thông tin 1 device
- `updateDevice(Device, DeviceCallback)`: Cập nhật device
- `deleteDevice(String deviceId, DeviceCallback)`: Xóa device

**Features**:
- Auto token refresh nếu expired
- Singleton pattern
- Async execution với ExecutorService

### DeviceDataService.java
**Package**: `com.example.smart_bin.api`

**Chức năng**:
- `fetchDeviceData(String deviceId, DeviceDataCallback)`: Lấy device data (waste counts, fill level, battery)

### NotificationService.java
**Package**: `com.e
**Package**: `com.example.smart_bin.bluetooth`

**Chức năng**:
- Scan và list paired Bluetooth devices
- Kết nối với ESP32-CAM qua **Bluetooth Classic** (SPP profile)
- Gửi WiFi credentials (SSID + password) format: `WIFI:ssid:password`
- Nhận response từ ESP32 (success/error)
- Auto disconnect sau khi gửi

**Methods**:
- `getPairedDevices()`: Lấy danh sách paired devices
- `connectToDevice(BluetoothDevice, BLECallback)`: Kết nối
- `sendWifiCredentials(String ssid, String password)`: Gửi WiFi info
- `disconnect()`: Ngắt kết nối

### WiFiScanner.java
**Package**: `com.example.smart_bin.wifi`

**Chức năng**:
- Scan các WiFi networks khả dụng (cần permission ACCESS_FINE_LOCATION)
- Show danh sách SSID để user chọn
- Truyền SSID đã chọn qua Bluetooth cho ESP32

**Methods**:
- `startScan(ScanCallback)`: Bắt đầu scan WiFi
- Callback trả về `List<String>` SSIDs
- `refreshToken()`: Refresh access token
- `logout()`: Đăng xuất
- `changePassword()`: Đổi mật khẩu
- `resendVerification()`: Gửi lại email xác thực

### TokenManager.java
**Package**: `com.example.smart_bin.utils`

**Chức năng**:
- Lưu/lấy access/refresh token từ SharedPreferences
- `isTokenValid()`: Check token còn hợp lệ
- `needsRefresh()`: Check cần refresh (expire trong < 5 phút)
- `clearTokens()`: Xóa tokens khi logout

---

## 📶 Bluetooth Integration

### BLEManager.java - Bluetooth Classic
**Chức năng**:
- Scan và list paired Bluetooth devices
- Kết nối với ESP32-CAM qua Bluetooth Classic (SPP)
- Gửi WiFi credentials (SSID + password)
- Nhận response từ ESP32

### WiFiScanner.java - WiFi Scanning
**Chức năng**:
- Scan các WiFi networks khả dụng
- Show danh sách để user chọn
- Truyền thông tin WiFi qua Bluetooth cho ESP32

---

## 📸 Screenshots

> TODO: Thêm screenshots

```
[Login Screen]
[Home - Device List]
[Device Detail]
[Add Device - Bluetooth]
[Notifications]
[Settings]
```

---

## 🔨 Build & Release

### Debug Build

**Command line:**
```bash
./gradlew assembleDebug
```

**Android Studio:**
- Build → Build Bundle(s) / APK(s) → Build APK(s)

**Output**: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build (Signed)

1. **Tạo Keystore** (lần đầu):
```bash
keytool -genkey -v -keystore smart-bin-release.keystore \
  -alias smart-bin -keyalg RSA -keysize 2048 -validity 10000
```

2. **Cấu hình signing trong `app/build.gradle.kts`**:
```kotlin
android {
    signingConfigs {
        create("release") {
            storeFile = file("../smart-bin-release.keystore")
            storePassword = "your_password"
            keyAlias = "smart-bin"
            keyPassword = "your_password"
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

3. **Build Release APK**:
```bash
./gradlew assembleRelease
```

**Output**: `app/build/outputs/apk/release/app-release.apk`

### Install APK

**Via ADB:**
```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Via Android Studio:**
- Run → Run 'app' → Chọn device/emulator

**Direct Install:**
- Copy APK file vào device
- Mở file → Install (enable "Install from Unknown Sources")

---

## 🧪 Testing

### Run on Emulator

1. **Tạo AVD** (Android Virtual Device):
   - Tools → Device Manager → Create Device
   - Chọn hardware (Pixel 6)
   - Chọn system image (API 34 - Android 14)
   - Finish

2. **Run app**:
   - Run → Run 'app'
   - Chọn emulator vừa tạo

### Run on Physical Device

1. **Enable Developer Options** trên device:
   - Settings → About Phone → Tap "Build number" 7 lần

2. **Enable USB Debugging**:
   - Settings → Developer Options → USB Debugging → ON

3. **Connect device**:
   - Cắm USB cable
   - Chấp nhận "Allow USB debugging"

4. **Run app**:
   - Run → Run 'app'
   - Chọn device

### Test Bluetooth Connection

1. **Pair ESP32** trước:
   - Settings → Bluetooth → Scan → Pair với "SmartBin_XXXXXX"

2. **Test trong app**:
   - Add Device → Chọn ESP32 từ paired devices
   - Nhập WiFi credentials
   - Check ESP32 serial monitor xem có nhận được

---

## 🐛 Common Issues

### 1. Không kết nối được server

**Lỗi**: `java.net.ConnectException: Failed to connect`

**Giải pháp**:
- Kiểm tra `BASE_URL` đúng IP server
- Không dùng `localhost` trên device/emulator
- Kiểm tra firewall server
- Dùng `adb logcat` để debug

### 2. Cleartext HTTP không được phép

**Lỗi**: `Cleartext HTTP traffic not permitted`

**Giải pháp**:
- Thêm `android:usesCleartextTraffic="true"` vào `<application>` trong `AndroidManifest.xml`
- Hoặc dùng `network_security_config.xml`

### 3. Bluetooth permission denied

**Lỗi**: `SecurityException: Need BLUETOOTH_SCAN permission`

**Giải pháp**:
- Request runtime permissions cho Android 12+
- Kiểm tra `AndroidManifest.xml` có đủ permissions

### 4. Token expired

**Lỗi**: `401 Unauthorized`

**Giải pháp**:
- TokenManager tự động refresh
- Nếu refresh fail → User phải login lại
- Check `refreshToken` còn hạn

---

## 📚 Tài liệu tham khảo

- [Android Developer Guide](https://developer.android.com/guide)
- [Material Design 3](https://m3.material.io/)
- [Bluetooth Classic Guide](https://developer.android.com/guide/topics/connectivity/bluetooth)
- [OkHttp Documentation](https://square.github.io/okhttp/)

---

## 👥 Contributors

- **Your Name** - Android Developer

---

<div align="center">
  <p>Made with ❤️ for Smart Bin System</p>
</div>