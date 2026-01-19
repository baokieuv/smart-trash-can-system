# 📷 Smart Bin ESP32-CAM Firmware

| Supported Targets | ESP32-S3 |
| ----------------- | -------- |

Firmware cho ESP32-CAM để nhận diện và phân loại rác thải tự động sử dụng camera, servo motor và ultrasonic sensor.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Hardware Requirements](#-hardware-requirements)
- [Pin Configuration](#-pin-configuration)
- [Components](#-components)
- [Yêu cầu](#-yêu-cầu)
- [Cài đặt ESP-IDF](#-cài-đặt-esp-idf)
- [Build & Flash](#-build--flash)
- [Configuration](#-configuration)
- [WiFi Setup](#-wifi-setup)
- [Bluetooth Pairing](#-bluetooth-pairing)
- [Operation Modes](#-operation-modes)
- [API Endpoints](#-api-endpoints)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Giới thiệu

Smart Bin ESP32-CAM firmware là phần mềm nhúng chạy trên ESP32-S3 CAM module, thực hiện các chức năng:

- **Camera**: Chụp ảnh rác thải và gửi lên server để phân loại
- **Ultrasonic Sensor**: Phát hiện rác và đo mức độ đầy của thùng
- **Servo Motor**: Điều khiển 3 nắp thùng (recyclable, compostable, non-recyclable)
- **WiFi**: Kết nối Internet để gửi dữ liệu
- **Bluetooth**: Cấu hình WiFi và đăng ký thiết bị
- **Deep Sleep**: Tiết kiệm năng lượng khi không sử dụng
- **LED/Buzzer**: Báo hiệu trạng thái thiết bị

---

## ✨ Tính năng

### 🔍 Waste Detection & Classification
- ✅ Tự động phát hiện rác qua ultrasonic sensor (< 10cm)
- ✅ Chụp ảnh rác và gửi lên server
- ✅ Nhận kết quả phân loại (recyclable/compostable/non-recyclable)
- ✅ Tự động mở nắp thùng tương ứng trong 5 giây
- ✅ Cập nhật thống kê số lượng rác theo loại

### 📊 Device Monitoring
- ✅ Đo mức độ đầy của thùng (fill level %)
- ✅ Kiểm tra pin (battery level - TODO)
- ✅ Gửi device data lên server định kỳ (10s)
- ✅ Theo dõi trạng thái online/offline

### 🔧 Configuration Modes
- ✅ **Normal Mode**: Hoạt động bình thường
- ✅ **Config Mode**: Cấu hình WiFi qua Bluetooth (double click button)
- ✅ **Reset Mode**: Xóa cấu hình và khởi tạo lại (long press button 3s)
- ✅ **Deep Sleep**: Tắt máy để tiết kiệm pin (single click button)

### 📡 Connectivity
- ✅ WiFi 802.11 b/g/n (2.4GHz)
- ✅ Bluetooth Classic (SPP profile)
- ✅ HTTP/HTTPS client
- ✅ NVS storage cho WiFi credentials

### 🔔 Alerts
- ✅ LED blink patterns (config, reset, error)
- ✅ Buzzer beep patterns (detection, success, error)
- ✅ Gửi notification khi thùng đầy (> 80%)

---

## 🛠️ Hardware Requirements

| Component | Model | Quantity | Purpose |
|-----------|-------|----------|---------|
| **ESP32-S3 CAM** | ESP32-S3-DevKitC-1 | 1 | Main controller + Camera |
| **Ultrasonic Sensor** | HC-SR04 | 2 | Distance measurement |
| **Servo Motor** | SG90 | 2 | Lid control |
| **LED** | Red LED | 1 | Status indicator |
| **Buzzer** | Active Buzzer | 1 | Sound alerts |
| **Push Button** | Tactile Switch | 1 | Mode control |
| **Power Supply** | 5V 2A | 1 | Power source |
| **Resistors** | 220Ω, 10kΩ | - | LED, Pull-up/down |
| **Breadboard & Wires** | - | - | Prototyping |

---

## 📌 Pin Configuration

### GPIO Pins

```c
// Camera (Built-in on ESP32-S3 CAM)
#define CAM_PIN_PWDN    -1
#define CAM_PIN_RESET   -1
#define CAM_PIN_XCLK    GPIO_NUM_15
#define CAM_PIN_SIOD    GPIO_NUM_4
#define CAM_PIN_SIOC    GPIO_NUM_5
// ... (more camera pins)

// Servo Motors (PWM)
#define SERVO_180_GPIO          GPIO_NUM_1
#define SERVO_360_GPIO          GPIO_NUM_2

// Ultrasonic Sensor (HC-SR04)
#define ULTRASONIC1_ECHO_PIN        GPIO_NUM_45
#define ULTRASONIC1_TRIG_PIN        GPIO_NUM_47

#define ULTRASONIC_ECHO_SHARED_PIN  GPIO_NUM_39
#define ULTRASONIC2_TRIG_PIN        GPIO_NUM_40

// LED & Buzzer
#define LED_STATUS_PIN          GPIO_NUM_14
#define BUZZER_PIN              GPIO_NUM_21

// Button (with internal pull-up)
#define BTN_CONFIG_PIN          GPIO_NUM_0 
```

---

## 🔧 Components

Firmware được chia thành các module chức năng:

### Core Components

| File | Description |
|------|-------------|
| `main.c` | Main application entry point |
| `config.h` | Configuration constants & macros |

### Managers

| Component | File | Purpose |
|-----------|------|---------|
| **Camera Handler** | `camera_handler.c/h` | Camera initialization & capture |
| **Servo Controller** | `servo.c/h` | Servo motor control (3 lids) |
| **Ultrasonic Sensor** | `ultrasonic_sensor.c/h` | Distance measurement (HC-SR04) |
| **WiFi Manager** | `wifi_manager.c/h` | WiFi connection & auto-reconnect |
| **Bluetooth Manager** | `bluetooth_manager.c/h` | BT SPP for configuration |
| **HTTP Client** | `http_client.c/h` | Send images & device data |
| **Waste Manager** | `waste_manager.c/h` | Track waste counts & fill level |
| **NVS Storage** | `nvs_storage.c/h` | Store WiFi credentials |
| **GPIO Handler** | `gpio_handler.c/h` | LED, Buzzer, Button control |

---

## 📱 Yêu cầu

### Software
- **ESP-IDF**: v5.0+ ([Installation Guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/get-started/))
- **Python**: 3.8+
- **Git**: Latest version

### Hardware
- ESP32-S3 Development Board with Camera
- USB cable (USB-C for ESP32-S3)
- Computer (Windows/Linux/macOS)

---

## 🚀 Cài đặt ESP-IDF

### 1. Cài đặt ESP-IDF (Windows)

```powershell
# Download ESP-IDF installer
# https://dl.espressif.com/dl/esp-idf/

# Or use git clone
git clone --recursive https://github.com/espressif/esp-idf.git
cd esp-idf
git checkout v5.3
git submodule update --init --recursive

# Run install script
.\install.bat esp32s3

# Setup environment
.\export.bat
```

### 2. Cài đặt ESP-IDF (Linux/macOS)

```bash
# Install prerequisites
sudo apt-get install git wget flex bison gperf python3 python3-pip python3-venv cmake ninja-build ccache libffi-dev libssl-dev dfu-util libusb-1.0-0

# Clone ESP-IDF
git clone --recursive https://github.com/espressif/esp-idf.git
cd esp-idf
git checkout v5.3
git submodule update --init --recursive

# Install
./install.sh esp32s3

# Setup environment (add to ~/.bashrc)
. $HOME/esp/esp-idf/export.sh
```

---

## 🔨 Build & Flash

### 1. Clone project

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/esp32-cam
```

### 2. Configure project

```bash
# Set target
idf.py set-target esp32s3

# Open menuconfig (optional)
idf.py menuconfig
```

### 3. Build firmware

```bash
# Build
idf.py build
```

### 4. Flash to ESP32

```bash
# Find COM port (Windows: COM3, Linux: /dev/ttyUSB0)
idf.py -p COM3 flash
```

### 5. Monitor serial output

```bash
idf.py -p COM3 monitor

# Or build + flash + monitor
idf.py -p COM3 flash monitor
```

---

## ⚙️ Configuration

### Constants trong `config.h`

```c
// WiFi
#define WIFI_RECONNECT_DELAY_MS    5000
#define WIFI_MAX_RETRY             5

// HTTP
#define HTTP_SERVER_URL            "http://192.168.1.100:8080"
#define HTTP_CLASSIFY_ENDPOINT     "/api/devices/classify"
#define HTTP_DEVICE_DATA_ENDPOINT  "/api/devices/data"

// Sensor
#define DISTANCE_THRESHOLD_CM      10.0
#define SENSOR_CHECK_INTERVAL_MS   2000
#define BIN_HEIGHT_CM              30.0

// Servo
#define SERVO_CLOSE_DEGREE         0
#define SERVO_OPEN_DEGREE          90
#define SERVO_HOLD_TIME_MS         5000

// Device Status
#define DEVICE_STATUS_INTERVAL_MS  10000
```

### Sửa `config.h` theo môi trường của bạn:

1. **HTTP_SERVER_URL**: IP của Spring Boot backend
2. **BIN_HEIGHT_CM**: Chiều cao thùng rác thực tế
3. **DISTANCE_THRESHOLD_CM**: Ngưỡng phát hiện rác

---

## 📡 WiFi Setup

### Method 1: Bluetooth Configuration (recommended)

1. **Kích hoạt Config Mode**:
   - Double-click button trên ESP32
   - LED sẽ blink 5 lần
   - Bluetooth SPP server bật

2. **Kết nối từ Android App**:
   - Scan Bluetooth devices
   - Kết nối với `SmartBin_XXXX`
   - Gửi lệnh: `WIFI:SSID:PASSWORD`

3. **Thiết bị tự kết nối WiFi**:
   - Credentials được lưu vào NVS
   - Tự động kết nối WiFi
   - Đăng ký với server

---

## 📲 Bluetooth Pairing

### Bluetooth Commands

ESP32 hỗ trợ các lệnh qua Bluetooth SPP:

| Command | Format | Example | Response |
|---------|--------|---------|----------|
| Set WiFi | `WIFI:<ssid>:<password>` | `WIFI:MyWiFi:12345678` | `OK:WIFI_SET` |
| Get Status | `STATUS` | `STATUS` | `{"wifi":"connected","ip":"192.168.1.50"}` |
| Reset | `RESET` | `RESET` | `OK:RESET` |

### Pairing Flow

```
Mobile App → Scan BT → Connect "SmartBin_XXXX"
          ↓
Mobile App → Send "WIFI:SSID:PASS"
          ↓
ESP32     → Save to NVS → Connect WiFi
          ↓
ESP32     → Register with Server → Send Device ID
          ↓
Server    → Create Device Record
          ↓
ESP32     → Restart in Normal Mode
```

---

## 🔄 Operation Modes

### 1. Normal Mode (Default)

- Sensor detection active
- WiFi connected
- Send device data every 10s
- Classify waste on detection
- Button: Single click → Deep Sleep

### 2. Config Mode (Double Click)

- WiFi disconnected
- Bluetooth enabled
- LED blink 5 times
- Accept WiFi credentials via BT
- Button: Double click again → Restart

### 3. Reset Mode (Long Press 3s)

- Clear all NVS data (WiFi, device ID)
- Enter Config Mode
- LED rapid blink
- Ready for re-setup

### 4. Deep Sleep Mode (Single Click)

- All peripherals off
- WiFi disconnected
- Wake on button press (GPIO 0)
- Ultra-low power consumption

---

## 🌐 API Endpoints

### 1. Classify Image

**POST** `http://SERVER_URL/api/devices/classify`

```http
Content-Type: multipart/form-data

image: [binary data]
deviceId: "AA_BB_CC_DD_EE_FF"
```

**Response:**

```json
{
  "label": "plastic",
  "confidence": 0.95,
  "category": "recyclable"
}
```

### 2. Send Device Data

**POST** `http://SERVER_URL/api/devices/data`

```http
Content-Type: application/json

{
  "deviceId": "AA_BB_CC_DD_EE_FF",
  "recyclableCount": 5,
  "compostableCount": 3,
  "nonRecyclableCount": 2,
  "fillLevel": 45.5,
  "batteryLevel": 85.0
}
```

**Response:**

```json
{
  "message": "Device data updated successfully"
}
```

---

## 🐛 Troubleshooting

### Camera Not Working

```
Camera init failed
```

**Solution:**
- Kiểm tra camera module có cắm chặt
- Verify pin connections
- Thử reset ESP32 (press EN button)
- Check camera power (3.3V)

### WiFi Won't Connect

```
WiFi: Failed to connect (max retries)
```

**Solution:**
- Verify SSID and password
- Check WiFi is 2.4GHz (not 5GHz)
- Move ESP32 closer to router
- Reset WiFi credentials (long press button)

### Bluetooth Not Visible

```
Bluetooth init failed
```

**Solution:**
- Ensure ESP32 is in Config Mode (double click)
- Check Bluetooth is enabled on phone
- Try restarting ESP32
- Update ESP-IDF to latest version

### Servo Not Moving

```
Servo init failed
```

**Solution:**
- Check servo connections (PWM pins)
- Verify 5V power supply (servos need 5V, not 3.3V)
- Test servo with simple sweep code
- Check GPIO pins not used by camera

### Ultrasonic Sensor No Reading

```
Ultrasonic: No echo received
```

**Solution:**
- Check Trig/Echo pin connections
- Verify sensor has 5V power
- Test sensor range (2cm - 400cm)
- Check for obstacles blocking sensor

### Device Not Registering

```
HTTP: Register device failed
```

**Solution:**
- Check server is running
- Verify HTTP_SERVER_URL in config.h
- Test server endpoint with curl
- Check device ID format (MAC address)

### Build Errors

```
fatal error: esp_camera.h: No such file or directory
```

**Solution:**
- Ensure `idf.py set-target esp32s3` was run
- Check ESP-IDF version (v5.0+)
- Update submodules: `git submodule update --init --recursive`
- Clean build: `idf.py fullclean`

---

## 📝 Serial Monitor Output

### Normal Operation

```
I (1234) SMART_BIN: Device started successfully
I (1235) WIFI: Connecting to WiFi...
I (3456) WIFI: Connected! IP: 192.168.1.50
I (3457) HTTP: Device registered successfully
I (5000) SENSOR: Ultrasonic: Waste detected at 8.50 cm
I (5100) CAMERA: Image captured (102400 bytes)
I (6200) HTTP: Classification: plastic (0.95) → recyclable
I (6201) SERVO: Opening recyclable lid
I (11201) SERVO: Closing recyclable lid
```

### Config Mode

```
I (1234) SMART_BIN: === ENTERING CONFIGURATION MODE ===
I (1235) WIFI: WiFi stopped
I (1500) BLUETOOTH: Bluetooth initialized
I (1501) BLUETOOTH: SPP server started, name: SmartBin_AA_BB_CC
I (2000) BLUETOOTH: Client connected
I (2100) BLUETOOTH: Received: WIFI:MyNetwork:MyPassword123
I (2101) NVS: WiFi credentials saved
I (2102) BLUETOOTH: Sent: OK:WIFI_SET
I (3000) SMART_BIN: Restarting in 3 seconds...
```

---

## 🔗 Related Documentation

- [ESP-IDF Programming Guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32s3/)
- [ESP32-CAM Guide](https://randomnerdtutorials.com/esp32-cam-video-streaming-face-recognition-arduino-ide/)
- [Backend API Documentation](../backend-springboot/README.md)
- [Android App Documentation](../android-app/README.md)

---

## 📄 License

MIT License - See [LICENSE](../LICENSE) file

---

## 👥 Contributors

- Development Team - Smart Bin System Project 3
