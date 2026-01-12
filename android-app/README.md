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
| **Android SDK** | API 24+ (Nougat 7.0+) | Platform |
| **Java** | 8+ | Programming language |
| **Material Design** | 3.x | UI components |
| **ViewBinding** | - | View access |
| **Retrofit** | 2.x (optional) | HTTP client |
| **OkHttp** | 4.x | HTTP/HTTPS requests |
| **Gson** | 2.x | JSON parsing |
| **RecyclerView** | - | List rendering |
| **SwipeRefreshLayout** | - | Pull to refresh |
| **Bluetooth Classic** | - | Device pairing |

---

## 📱 Yêu cầu

### Minimum Requirements
- **Android Version**: 7.0 (API 24) trở lên
- **RAM**: 2GB
- **Storage**: 50MB free space
- **Bluetooth**: Bluetooth Classic support
- **Network**: WiFi hoặc Mobile Data

### Recommended
- **Android Version**: 10.0 (API 29) trở lên
- **RAM**: 4GB
- **Bluetooth**: Bluetooth 5.0+

---

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/your-username/smart-bin-system.git
cd smart-bin-system/android-app
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

## ⚙️ Cấu hình

### Network Security Config

File: `app/src/main/res/xml/network_security_config.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">YOUR_SERVER_IP</domain>
        <domain includeSubdomains="true">localhost</domain>
    </domain-config>
</network-security-config>
```

### Gradle Dependencies

File: `app/build.gradle`

```gradle
dependencies {
    // AndroidX
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.11.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.swiperefreshlayout:swiperefreshlayout:1.1.0'
    
    // Preferences
    implementation 'androidx.preference:preference:1.2.1'
    
    // JSON parsing
    implementation 'com.google.code.gson:gson:2.10.1'
    
    // Optional: Retrofit (nếu muốn dùng thay OkHttp)
    // implementation 'com.squareup.retrofit2:retrofit:2.9.0'
    // implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
}
```

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

### Pattern: MVC + Repository

```
┌─────────────┐
│   Activity  │ ◄─── User interactions
│  / Fragment │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│   Service   │ ◄─── Business logic
│  (ApiService│      (Auth, Device CRUD)
│  AuthService)
└──────┬──────┘
       │
       ▼
┌─────────────┐
│  Repository │ ◄─── Data source
│   (Network  │      (HTTP calls)
│    + Cache) │
└──────┬──────┘
       │
       ▼
┌─────────────┐
│    Model    │ ◄─── Data objects
│ (POJO/DTO)  │
└─────────────┘
```

### Token Management Flow

```
1. User login → AuthService.login()
2. Server returns: accessToken + refreshToken
3. TokenManager.saveAuthResponse() → SharedPreferences
4. Every API call:
   - Check token expiry
   - If expired → call AuthService.refreshToken()
   - Retry original request with new token
5. If refresh fails → Navigate to LoginActivity
```

---

## 📡 API Integration

### ApiService.java

Xử lý tất cả API calls liên quan đến devices và data.

#### Các method chính:

```java
// Device APIs
public void fetchDevices(DevicesCallback callback)
public void createDevice(Device device, DeviceCallback callback)
public void getDevice(String deviceId, DeviceCallback callback)
public void updateDevice(Device device, DeviceCallback callback)
public void deleteDevice(String deviceId, DeviceCallback callback)

// Device Data
public void fetchDeviceData(String deviceId, DeviceDataCallback callback)

// Notifications
public void fetchNotifications(NotificationCallback callback)
```

#### Ví dụ sử dụng:

```java
ApiService.getInstance(context).fetchDevices(new ApiService.DevicesCallback() {
    @Override
    public void onSuccess(List<Device> devices) {
        // Update UI với danh sách devices
        adapter.setDevices(devices);
    }

    @Override
    public void onError(String error) {
        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
    }
});
```

### AuthService.java

Xử lý authentication APIs.

#### Các method chính:

```java
public void register(String email, String password, String firstName, 
                    String lastName, MessageCallback callback)
public void login(String email, String password, AuthCallback callback)
public void refreshToken(RefreshCallback callback)
public void logout(MessageCallback callback)
public void changePassword(String currentPassword, String newPassword, 
                          String confirmPassword, MessageCallback callback)
public void resendVerification(String email, MessageCallback callback)
```

### TokenManager.java

Quản lý JWT tokens trong SharedPreferences.

```java
// Save tokens after login
tokenManager.saveAuthResponse(accessToken, refreshToken, expiresIn, 
                              userId, email, firstName, lastName, emailVerified);

// Check if logged in
boolean isLoggedIn = tokenManager.isLoggedIn();

// Check if token needs refresh
boolean needsRefresh = tokenManager.needsRefresh();

// Get access token
String token = tokenManager.getAccessToken();

// Clear tokens (logout)
tokenManager.clearTokens();
```

---

## 📶 Bluetooth Integration

### BLEManager.java

Quản lý Bluetooth Classic connection với ESP32-CAM.

#### Kết nối device:

```java
BLEManager bleManager = new BLEManager(context);

// Get paired devices
Set<BluetoothDevice> pairedDevices = bleManager.getPairedDevices();

// Connect to device
bleManager.connectToDevice(device, new BLEManager.BLECallback() {
    @Override
    public void onConnected() {
        // Device connected
    }

    @Override
    public void onDisconnected() {
        // Device disconnected
    }

    @Override
    public void onDataSentSuccess() {
        // WiFi credentials sent
    }

    @Override
    public void onError(String error) {
        Log.e(TAG, "Error: " + error);
    }
});
```

#### Gửi WiFi credentials:

```java
bleManager.sendWifiCredentials(ssid, password);
```

### WiFiScanner.java

Scan WiFi networks để chọn khi config ESP32.

```java
WiFiScanner scanner = new WiFiScanner(context);

scanner.startScan(new WiFiScanner.ScanCallback() {
    @Override
    public void onScanCompleted(List<String> networks) {
        // Show networks in dialog
        showNetworkSelectionDialog(networks);
    }

    @Override
    public void onScanFailed(String error) {
        Toast.makeText(context, error, Toast.LENGTH_SHORT).show();
    }
});
```

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

```bash
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`

### Release Build

#### 1. Tạo keystore

```bash
keytool -genkey -v -keystore smartbin-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias smartbin
```

#### 2. Cấu hình signing

File: `app/build.gradle`

```gradle
android {
    signingConfigs {
        release {
            storeFile file("../smartbin-release.jks")
            storePassword "your_store_password"
            keyAlias "smartbin"
            keyPassword "your_key_password"
        }
    }
    
    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 
                         'proguard-rules.pro'
            signingConfig signingConfigs.release
        }
    }
}
```

#### 3. Build release

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/app-release.apk`

#### 4. Upload lên Google Play Store

- Tạo listing trên [Google Play Console](https://play.google.com/console)
- Upload APK hoặc AAB
- Điền thông tin app
- Submit for review

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