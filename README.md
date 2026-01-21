# 🗑️ Smart Bin System

> Hệ thống phân loại rác thông minh sử dụng AI (YOLOv11n-cls) để tự động nhận diện và phân loại rác thải

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16.0.7-black.svg)](https://nextjs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-Latest-009688.svg)](https://fastapi.tiangolo.com/)
[![Android](https://img.shields.io/badge/Android-7.0+-green.svg)](https://developer.android.com/)
[![ESP-IDF](https://img.shields.io/badge/ESP--IDF-v5.0+-blue.svg)](https://docs.espressif.com/projects/esp-idf/)

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Tính năng chính](#-tính-năng-chính)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Quick Start với Docker](#-quick-start-với-docker)
- [Cài đặt từng service](#-cài-đặt-từng-service)
- [Documentation](#-documentation)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Giới thiệu

**Smart Bin System** là giải pháp IoT toàn diện giúp tự động hóa việc phân loại rác thải sử dụng AI. Hệ thống kết hợp phần cứng ESP32-CAM với mô hình AI YOLOv11n-cls để nhận diện và phân loại rác thải tự động, giúp nâng cao hiệu quả tái chế và bảo vệ môi trường.

### 🔹 Nhóm phân loại

Hệ thống nhận diện **10 loại rác** và tự động phân loại vào **3 nhóm chính**:

| Nhóm | Loại rác | Biểu tượng | Mô tả |
|------|----------|------------|-------|
| **♻️ Recyclable** | `cardboard`, `paper`, `plastic`, `metal`, `glass` | 🟢 | Có thể tái chế |
| **🌱 Compostable** | `biological`, `clothes`, `shoes` | 🟡 | Phân hủy sinh học |
| **🚫 Non-recyclable** | `battery`, `trash` | 🔴 | Không tái chế được |

### 🎯 Đối tượng sử dụng

- 🏠 **Hộ gia đình**: Quản lý rác thải thông minh tại nhà
- 🏫 **Trường học**: Giáo dục ý thức phân loại rác
- 🏢 **Văn phòng**: Nâng cao hiệu quả quản lý chất thải
- 🏛️ **Khu công cộng**: Phân loại rác tự động cho cộng đồng
- 🏭 **Tổ chức, doanh nghiệp**: Giảm thiểu chất thải, bảo vệ môi trường

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           SMART BIN SYSTEM                              │
│                        (Microservices Architecture)                     │
└─────────────────────────────────────────────────────────────────────────┘

┌──────────────┐                                        ┌──────────────┐
│   Next.js    │ ◄────── REST API ───────┐              │   FastAPI    │
│  (Web App)   │                         │              │  (AI Model)  │
│  Port: 3000  │                         │              │  Port: 8000  │
└──────────────┘                         │              └──────┬───────┘
                                         │                     │
                                  ┌──────▼────────┐     ┌─────▼────────┐
┌──────────────┐                  │  Spring Boot  │────►│ YOLOv11n-cls │
│ Android App  │ ◄─── REST API ───┤    Backend    │     │ ONNX Runtime │
│   (Mobile)   │                  │  Port: 8888   │     └──────────────┘
└──────────────┘                  └──────┬────────┘
                                         │
┌──────────────┐                         │
│  ESP32-CAM   │ ◄─── HTTP/WiFi ─────────┤
│  (Hardware)  │                         │
│ + 2 Servos   │                         │
└──────────────┘                  ┌──────▼────────┐
                                  │               │
                           ┌──────┴──────┐ ┌──────┴──────┐
                           │   MariaDB   │ │    MinIO    │
                           │ (Database)  │ │  (Storage)  │
                           │ Port: 3333  │ │ Port: 9000  │
                           └─────────────┘ └─────────────┘

┌─────────────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE SERVICES                          │
├─────────────────────────────────────────────────────────────────────────┤
│  Keycloak (OAuth2)  │  PostgreSQL (Keycloak DB)  │  Redis (Cache)       │
│  Port: 8080         │  Port: 5432                │  Port: 6379          │
└─────────────────────────────────────────────────────────────────────────┘
```

### 📊 Luồng hoạt động chính:

1. **🎥 Phát hiện rác**: ESP32-CAM dùng cảm biến siêu âm HC-SR04 phát hiện rác
2. **📸 Chụp ảnh**: Camera chụp ảnh rác thải với độ phân giải 640x480
3. **📤 Gửi lên server**: ESP32 gửi ảnh qua WiFi/HTTP đến Spring Boot Backend
4. **🤖 AI phân loại**: Backend gọi FastAPI để phân tích ảnh với YOLOv11n-cls
5. **💾 Lưu dữ liệu**: Kết quả được lưu vào MariaDB, ảnh lưu vào MinIO
6. **🔧 Điều khiển servo**: ESP32 nhận kết quả và mở nắp thùng phù hợp (3 servo motors)
7. **📱 Cập nhật UI**: Web/Mobile app hiển thị thống kê real-time
8. **🔔 Thông báo**: Gửi notification khi thùng đầy hoặc có lỗi

### 🔐 Authentication Flow:

```
User ─► Frontend/Mobile ─► Spring Boot ─► Keycloak
                             │  (OAuth2)    │
                             │◄─────────────┘
                             │  JWT Token
                             ▼
                         Authorized API
```

---

## 🚀 Công nghệ sử dụng

### 🌐 Frontend - Web Application

| Technology | Version | Purpose |
|------------|---------|---------|
| **Next.js** | 16.0.7 | React framework với App Router |
| **React** | 19.2.0 | UI library |
| **TypeScript** | 5.x | Type-safe development |
| **Tailwind CSS** | 4.x | Modern CSS framework |
| **Lucide React** | 0.555.0 | Icon library |

### 📱 Mobile Application

| Technology | Version | Purpose |
|------------|---------|---------|
| **Android SDK** | 24-36 | Platform support (Android 7.0-15) |
| **Java** | 11 | Primary programming language |
| **Kotlin** | 2.1.0 | Modern Android development |
| **Gradle** | 8.10 | Build automation |
| **Material Design** | 3.x | UI components |
| **Gson** | 2.10.1 | JSON serialization |
| **OkHttp** | 4.12.0 | HTTP client |
| **Retrofit** | 2.9.0 | REST API client |

### ⚙️ Backend Service

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 4.0.0 | Main API framework |
| **Java** | 21 | Runtime environment |
| **Spring Security** | 6.x | OAuth2 + JWT authentication |
| **Spring Data JPA** | Latest | Database ORM |
| **MariaDB** | 11.x | Primary database |
| **PostgreSQL** | 16.x | Keycloak database |
| **Keycloak** | 25.0 | Identity & access management |
| **MinIO** | Latest | S3-compatible object storage |
| **Redis** | Latest | Caching layer |
| **JavaMail** | Latest | Email service |

### 🤖 AI/ML Service

| Technology | Version | Purpose |
|------------|---------|---------|
| **FastAPI** | Latest | High-performance API framework |
| **Python** | 3.8+ | Runtime |
| **YOLOv11n-cls** | Latest | Image classification model |
| **ONNX Runtime** | Latest | Optimized model inference |
| **OpenCV** | Latest | Image processing |
| **Ultralytics** | Latest | YOLO training framework |
| **NumPy** | Latest | Numerical computing |
| **Pillow** | Latest | Image handling |

### 🔧 Hardware & Firmware

| Component | Model/Version | Purpose |
|-----------|---------------|---------|
| **Microcontroller** | ESP32-S3-WROOM-1 | Main controller |
| **Camera** | OV2640 (ESP32-CAM) | Image capture |
| **Ultrasonic Sensor** | HC-SR04 | Distance measurement |
| **Servo Motors** | SG90 x3 | Lid control (3 compartments) |
| **LED** | RGB LED | Status indicator |
| **Buzzer** | Passive buzzer | Audio alerts |
| **ESP-IDF** | v5.0+ | Development framework |
| **FreeRTOS** | Built-in | Real-time OS |

### 🐳 DevOps & Infrastructure

| Technology | Purpose |
|------------|---------|
| **Docker** | Containerization |
| **Docker Compose** | Multi-container orchestration |
| **Maven** | Java dependency management |
| **npm** | JavaScript package manager |
| **Git** | Version control |

---

## ✨ Tính năng chính

### 🌐 Web Application (Next.js)

#### 🔐 Authentication & User Management
- ✅ Đăng ký tài khoản mới với email verification
- ✅ Đăng nhập với Keycloak OAuth2/JWT
- ✅ Quên mật khẩu và reset password
- ✅ Đổi mật khẩu trong tài khoản
- ✅ Auto refresh token để duy trì session
- ✅ Persistent login với localStorage
- ✅ Logout an toàn (revoke token)

#### 📊 Dashboard & Statistics
- ✅ Tổng quan hệ thống (số thiết bị, tổng rác đã phân loại)
- ✅ Thống kê theo nhóm rác (Recyclable, Compostable, Non-recyclable)
- ✅ Biểu đồ phân tích xu hướng
- ✅ Tỷ lệ lấp đầy trung bình của các thùng

#### 🎛️ Device Management
- ✅ Danh sách tất cả thiết bị Smart Bin
- ✅ Thêm thiết bị mới (via device code)
- ✅ Xem chi tiết thiết bị (serial number, MAC address, status)
- ✅ Chỉnh sửa thông tin thiết bị (name, location)
- ✅ Xóa thiết bị
- ✅ Theo dõi trạng thái online/offline
- ✅ Xem fill level và battery status

#### 📜 History & Logs
- ✅ Lịch sử phân loại rác chi tiết
- ✅ Filter theo device, type, category, date range
- ✅ Xem ảnh rác đã phân loại
- ✅ Export dữ liệu (CSV, Excel)

#### 🔔 Notifications
- ✅ Thông báo khi thùng đầy (>80%)
- ✅ Cảnh báo lỗi thiết bị
- ✅ Thông báo offline device
- ✅ Đánh dấu đã đọc/chưa đọc
- ✅ Real-time updates

#### 💅 UI/UX Features
- ✅ Responsive design (mobile, tablet, desktop)
- ✅ Dark mode support (coming soon)
- ✅ Loading states và error handling
- ✅ Toast notifications
- ✅ Modern UI với Tailwind CSS
- ✅ Icon library với Lucide React

---

### 📱 Mobile Application (Android)

#### 🔐 Authentication
- ✅ Đăng ký tài khoản mới
- ✅ Đăng nhập với Keycloak OAuth2
- ✅ Auto login với saved credentials
- ✅ Logout và clear session

#### 📊 Dashboard
- ✅ Tổng quan thống kê real-time
- ✅ Số lượng thiết bị đang hoạt động
- ✅ Tổng rác đã phân loại
- ✅ Thống kê theo category
- ✅ Auto-refresh mỗi 30 giây

#### 🎛️ Device Management
- ✅ Danh sách thiết bị với filter
- ✅ **Thêm thiết bị qua Bluetooth LE**
- ✅ **Cấu hình WiFi cho ESP32-CAM qua Bluetooth**
- ✅ **Device pairing với OTP verification**
- ✅ Xem chi tiết thiết bị (fill level, battery, waste stats)
- ✅ Xóa thiết bị
- ✅ Pull-to-refresh

#### 🔔 Notifications
- ✅ Push notifications (Firebase Cloud Messaging)
- ✅ Thông báo thùng đầy
- ✅ Cảnh báo lỗi device
- ✅ Notification history

#### 🎨 UI/UX
- ✅ Material Design 3
- ✅ Dark/Light theme support
- ✅ Smooth animations
- ✅ Intuitive navigation
- ✅ Empty states và error handling

---

### 🤖 AI Model Service (FastAPI)

#### 🔍 Image Classification
- ✅ Nhận diện 10 loại rác với độ chính xác cao
- ✅ YOLOv11n-cls model (optimized for edge devices)
- ✅ ONNX Runtime inference (fast & efficient)
- ✅ Confidence score threshold (>0.85)
- ✅ Automatic category mapping (Recyclable/Compostable/Non-recyclable)

#### 🚀 Performance
- ✅ Fast inference time (~70ms per image)
- ✅ Batch processing support
- ✅ Image preprocessing (resize, normalize)
- ✅ Memory efficient

#### 📡 API Features
- ✅ RESTful endpoint: `POST /classify`
- ✅ Multipart file upload
- ✅ CORS enabled
- ✅ Error handling với HTTP status codes
- ✅ Input validation (file size, format)

---

### 🔧 Backend Service (Spring Boot)

#### 🔐 Authentication & Authorization
- ✅ Tích hợp Keycloak OAuth2 Resource Server
- ✅ JWT token validation
- ✅ Role-based access control (USER, ADMIN)
- ✅ User registration với email verification
- ✅ Password management (change, forgot password)
- ✅ Token refresh mechanism
- ✅ Logout với token revocation

#### 🎛️ Device Management APIs
- ✅ CRUD operations cho devices
- ✅ Device registration với unique device code
- ✅ Device status tracking (online/offline)
- ✅ Real-time device data updates
- ✅ Fill level monitoring
- ✅ Battery level tracking
- ✅ Location management

#### 📸 Image Processing
- ✅ Image upload với validation
- ✅ Store images trong MinIO (S3-compatible)
- ✅ Call FastAPI để classify images
- ✅ Save classification results với metadata
- ✅ Retrieve classified images

#### 📊 Device Data & Statistics
- ✅ Store waste classification records
- ✅ Calculate statistics (total waste, by category, by device)
- ✅ Aggregated data queries
- ✅ Date range filtering
- ✅ Export functionality

#### 🔔 Notification System
- ✅ Create notifications (full bin, device error)
- ✅ Mark as read/unread
- ✅ Delete notifications
- ✅ Real-time notification delivery
- ✅ Email notifications

#### ⏰ Scheduled Tasks
- ✅ Check device status định kỳ (mỗi 5 phút)
- ✅ Auto-detect offline devices
- ✅ Clean up old data (optional)
- ✅ Send summary reports (optional)

---

### 🎛️ Hardware & Firmware (ESP32-CAM)

#### 📸 Image Capture
- ✅ OV5640 camera với resolution 640x480
- ✅ Auto white balance và exposure
- ✅ JPEG compression
- ✅ Buffer management

#### 📡 Connectivity
- ✅ WiFi 2.4GHz connection
- ✅ HTTP client để gửi ảnh
- ✅ **Bluetooth LE** cho device setup
- ✅ Auto-reconnect khi mất kết nối
- ✅ DNS resolution

#### 🔧 Sensor Integration
- ✅ **HC-SR04 Ultrasonic sensor** (phát hiện rác)
  - Trigger: GPIO 47
  - Echo: GPIO 45
  - Detection range: 2-400cm
  - Threshold: < 15cm để detect rác
- ✅ **Fill level measurement** (đo độ đầy thùng)
  - Dùng ultrasonic đo khoảng cách
  - Calculate percentage: `(max - current) / max * 100%`

#### 🎯 Main Operation Flow
```
1. Idle mode → Deep sleep để tiết kiệm pin
2. Wake up mỗi 1s để check ultrasonic
3. Detect waste (distance < 15cm) → Beep + LED blue
4. Capture image với camera
5. Send image qua HTTP POST đến Backend
6. Receive classification result (category + label)
7. Open servo tương ứng (0-90°) trong 3s
8. Close servo (90-0°)
9. Update device status (waste count, fill level)
10. Send device data đến Backend
11. Return to idle/deep sleep
```

#### 🔧 Configuration
- ✅ **Bluetooth LE setup mode** cho WiFi credentials
- ✅ Device code registration
- ✅ OTA (Over-The-Air) firmware update (planned)
- ✅ Serial console debugging (115200 baud)

#### 📊 Device Monitoring
- ✅ Send heartbeat mỗi 10 giây
- ✅ Report fill level, battery level
- ✅ WiFi signal strength (RSSI)
- ✅ Uptime tracking
- ✅ Error reporting

---

## 📂 Cấu trúc dự án

```
smart-bin-system/
│
├── 📁 model-fastapi/              # AI Model Service (Python + FastAPI)
│
├── 📁 backend-springboot/         # Backend API (Spring Boot + Java 21)
│
├── 📁 frontend-nextjs/            # Web App (Next.js 16 + React 19)
│
├── 📁 android-app/               # Mobile App (Android + Java/Kotlin)
│
├── 📁 esp32-cam/                 # ESP32-CAM Firmware (C + ESP-IDF)
│
├── 📁 docker/                    # Docker Infrastructure
│
├── README.md                    # 📄 Main documentation (this file)
├── LICENSE                      # MIT License
└── .gitignore                   # Git ignore rules
```

---

## 🚀 Quick Start với Docker

### Yêu cầu hệ thống

- **Docker** >= 20.10
- **Docker Compose** >= 2.0
- **Git** (để clone repository)
- Ít nhất **4GB RAM** và **10GB disk space**

### Bước 1: Clone repository

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system
```

### Bước 2: Cấu hình environment variables

```bash
cd docker
cp .env.example .env    # Nếu có file example
nano .env               # Hoặc sử dụng editor khác
```

**File `.env` mẫu:**

```env
# MariaDB
MARIADB_ROOT_PASSWORD=rootpassword
MARIADB_DATABASE=smart_bin_db
MARIADB_USER=smartbin
MARIADB_PASSWORD=smartbin123

# PostgreSQL (for Keycloak)
POSTGRES_DB=keycloak_db
POSTGRES_USER=keycloak
POSTGRES_PASSWORD=keycloak123

# Keycloak
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin

# MinIO
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin

# Spring Boot
SPRING_DATASOURCE_URL=jdbc:mariadb://mariadb:3306/smart_bin_db
SPRING_DATASOURCE_USERNAME=smartbin
SPRING_DATASOURCE_PASSWORD=smartbin123

# FastAPI Model
MODEL_API_URL=http://model:8000

# Email (for verification)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### Bước 3: Khởi động tất cả services

```bash
# Khởi động tất cả containers
docker-compose up -d --build

# Xem logs của tất cả services
docker-compose logs -f

# Kiểm tra trạng thái containers
docker-compose ps
```

**Expected output:**

```
NAME                COMMAND                  SERVICE     STATUS      PORTS
mariadb             "docker-entrypoint.s…"   mariadb     running     0.0.0.0:3333->3306/tcp
postgres            "docker-entrypoint.s…"   postgres    running     5432/tcp
keycloak            "/opt/keycloak/bin/k…"   keycloak    running     0.0.0.0:8080->8080/tcp
redis               "docker-entrypoint.s…"   redis       running     0.0.0.0:6379->6379/tcp
minio               "/usr/bin/docker-ent…"   minio       running     0.0.0.0:9000-9001->9000-9001/tcp
model               "uvicorn server:app …"   model       running     0.0.0.0:8000->8000/tcp
backend             "java -jar app.jar"      backend     running     0.0.0.0:8888->8888/tcp
frontend            "node server.js"         frontend    running     0.0.0.0:3000->3000/tcp
```

### Bước 4: Truy cập các services

| Service | URL | Credentials |
|---------|-----|-------------|
| **Web App** | http://localhost:3000 | Đăng ký mới |
| **Backend API** | http://localhost:8888 | - |
| **FastAPI** | http://localhost:8000 | - |
| **FastAPI Docs** | http://localhost:8000/docs | - |
| **Keycloak** | http://localhost:8080 | admin / admin |
| **MinIO Console** | http://localhost:9001 | minioadmin / minioadmin |

### Bước 5: Thiết lập Keycloak (chỉ lần đầu)

#### 5.1. Truy cập Keycloak Admin Console

```
URL: http://localhost:8080
Username: admin
Password: admin
```

#### 5.2. Tạo Realm

1. Click dropdown **"Master"** ở góc trên bên trái
2. Click **"Create realm"**
3. Nhập:
   - **Realm name**: `smart-bin`
   - **Enabled**: ON
4. Click **"Create"**

#### 5.3. Tạo Client

1. Vào **Clients** → Click **"Create client"**
2. **General Settings**:
   - **Client ID**: `smart-bin-client`
   - **Client Protocol**: `openid-connect`
3. **Capability config**:
   - **Client authentication**: ON
   - **Authorization**: OFF
   - **Standard flow**: ON
   - **Direct access grants**: ON (for password grant)
4. **Login settings**:
   - **Valid redirect URIs**: `http://localhost:3000/*`
   - **Web origins**: `http://localhost:3000`
5. Click **"Save"**

#### 5.4. Lấy Client Secret

1. Vào **Clients** → `smart-bin-client` → Tab **"Credentials"**
2. Copy **Client Secret** (ví dụ: `abc123def456...`)
3. Cập nhật vào Spring Boot config

### Bước 6: Đăng ký tài khoản

1. Truy cập http://localhost:3000
2. Click **"Register"**
3. Điền thông tin:
   - Username
   - Email
   - Password
   - First Name / Last Name
4. Click **"Register"**
5. Kiểm tra email để verify (nếu cấu hình SMTP)
6. Đăng nhập với tài khoản vừa tạo

### Bước 7: Dừng và xóa containers (khi cần)

```bash
# Dừng containers
docker-compose stop

# Dừng và xóa containers (giữ lại data)
docker-compose down

# Xóa containers và volumes (xóa hết data)
docker-compose down -v
```

### Bước 8: Rebuild khi có thay đổi code

```bash
# Rebuild specific service
docker-compose build backend

# Rebuild all services
docker-compose build

# Rebuild and restart
docker-compose up -d --build
```

---

## 🔧 Cài đặt từng service

Nếu bạn không muốn dùng Docker, có thể chạy từng service riêng lẻ theo thứ tự sau:

### 1️⃣ FastAPI Model Service

**Yêu cầu**: Python 3.8+, pip

```bash
cd model-fastapi

# Tạo virtual environment
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate

# Cài đặt dependencies
pip install -r requirements.txt

# Chạy server
uvicorn server:app --host 0.0.0.0 --port 8000 --reload
```

**Test API:**

```bash
curl http://localhost:8000/
curl -X POST -F "file=@test_image.jpg" http://localhost:8000/classify
```

**Chi tiết**: Xem [model-fastapi/README.md](model-fastapi/README.md)

---

### 2️⃣ Spring Boot Backend

**Yêu cầu**: 
- Java 21 (JDK 21)
- Maven 3.8+
- MariaDB 11+
- PostgreSQL 16+ (cho Keycloak)
- Keycloak 25.0

#### 2.1. Cài đặt databases

**MariaDB:**

```bash
# Install MariaDB
sudo apt install mariadb-server   # Ubuntu/Debian
brew install mariadb               # macOS

# Start service
sudo systemctl start mariadb       # Linux
brew services start mariadb        # macOS

# Create database
mysql -u root -p
CREATE DATABASE smart_bin_db;
CREATE USER 'smartbin'@'localhost' IDENTIFIED BY 'smartbin123';
GRANT ALL PRIVILEGES ON smart_bin_db.* TO 'smartbin'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

**PostgreSQL:**

```bash
# Install PostgreSQL
sudo apt install postgresql postgresql-contrib   # Ubuntu/Debian
brew install postgresql@16                       # macOS

# Start service
sudo systemctl start postgresql    # Linux
brew services start postgresql@16  # macOS

# Create database for Keycloak
sudo -u postgres psql
CREATE DATABASE keycloak_db;
CREATE USER keycloak WITH PASSWORD 'keycloak123';
GRANT ALL PRIVILEGES ON DATABASE keycloak_db TO keycloak;
\q
```

#### 2.2. Cài đặt và cấu hình Keycloak

```bash
# Download Keycloak
wget https://github.com/keycloak/keycloak/releases/download/25.0.0/keycloak-25.0.0.zip
unzip keycloak-25.0.0.zip
cd keycloak-25.0.0

# Set admin credentials
export KEYCLOAK_ADMIN=admin
export KEYCLOAK_ADMIN_PASSWORD=admin

# Configure database (edit conf/keycloak.conf)
db=postgres
db-url=jdbc:postgresql://localhost:5432/keycloak_db
db-username=keycloak
db-password=keycloak123

# Start Keycloak
bin/kc.sh start-dev
```

Truy cập: http://localhost:8080

#### 2.3. Chạy Spring Boot

```bash
cd backend-springboot

# Cấu hình application.properties
nano src/main/resources/application.properties

# Build project
mvn clean install -DskipTests

# Run application
mvn spring-boot:run

# Hoặc run JAR
java -jar target/smart-bin-server-0.0.1-SNAPSHOT.jar
```

**Test API:**

```bash
curl http://localhost:8888/actuator/health
curl http://localhost:8888/api/auth/health
```

**Chi tiết**: Xem [backend-springboot/README.md](backend-springboot/README.md)

---

### 3️⃣ Next.js Frontend

**Yêu cầu**: Node.js 20+, npm hoặc yarn

```bash
cd frontend-nextjs

# Cài đặt dependencies
npm install
# hoặc
yarn install

# Cấu hình environment variables
cp .env.example .env.local
nano .env.local

# Chạy development server
npm run dev
# hoặc
yarn dev

# Build production
npm run build
npm run start
```

**File `.env.local`:**

```env
NEXT_PUBLIC_API_URL=http://localhost:8888
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8080
NEXT_PUBLIC_KEYCLOAK_REALM=smart-bin-realm
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=smart-bin-client
```

**Truy cập**: http://localhost:3000

**Chi tiết**: Xem [frontend-nextjs/README.md](frontend-nextjs/README.md)

---

### 4️⃣ Android Mobile App

**Yêu cầu**: 
- Android Studio Hedgehog | 2023.1.1+
- Android SDK 24-36 (Android 7.0 - 15)
- Gradle 8.10

#### 4.1. Import project

```bash
# Clone repository (nếu chưa)
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/android-app

# Open với Android Studio
# File → Open → Select android-app folder
```

#### 4.2. Cấu hình API URL

Sửa file `app/src/main/java/com/example/smartbinapp/utils/Constants.java`:

```java
public class Constants {
    // Sử dụng IP của máy chủ backend (không dùng localhost)
    public static final String BASE_URL = "http://192.168.1.100:8888";
    
    // Hoặc dùng ngrok nếu test từ xa
    // public static final String BASE_URL = "https://your-ngrok-url.ngrok.io";
}
```

#### 4.3. Build và chạy

```bash
# Sync Gradle
./gradlew build

# Build debug APK
./gradlew assembleDebug

# Install to device
./gradlew installDebug

# Run from Android Studio
# Click "Run" button hoặc Shift+F10
```

**Output APK**: `app/build/outputs/apk/debug/app-debug.apk`

**Chi tiết**: Xem [android-app/README.md](android-app/README.md)

---

### 5️⃣ ESP32-CAM Firmware

**Yêu cầu**:
- ESP-IDF v5.0 hoặc mới hơn
- Python 3.8+
- ESP32-S3 board với camera
- USB cable để flash

#### 5.1. Cài đặt ESP-IDF

**Linux/macOS:**

```bash
# Clone ESP-IDF
mkdir -p ~/esp
cd ~/esp
git clone -b v5.3 --recursive https://github.com/espressif/esp-idf.git

# Install tools
cd ~/esp/esp-idf
./install.sh esp32s3

# Set environment
. $HOME/esp/esp-idf/export.sh

# Add to .bashrc or .zshrc để tự động load
echo 'alias get_idf=". $HOME/esp/esp-idf/export.sh"' >> ~/.bashrc
```

**Windows:**

1. Download ESP-IDF Windows Installer: https://dl.espressif.com/dl/esp-idf/
2. Run installer và chọn ESP32-S3
3. Open "ESP-IDF PowerShell" hoặc "ESP-IDF CMD"

#### 5.2. Cấu hình project

```bash
cd esp32-cam

# Set target
idf.py set-target esp32s3

# Configure (optional)
idf.py menuconfig
# → Component config → ESP32-specific → Support for external RAM
# → Component config → Camera configuration
```

#### 5.3. Build và flash

```bash
# Build firmware
idf.py build

# Flash to device (auto-detect port)
idf.py -p auto flash

# Hoặc chỉ định port
idf.py -p COM3 flash          # Windows
idf.py -p /dev/ttyUSB0 flash  # Linux

# Monitor serial output
idf.py -p auto monitor

# Build + Flash + Monitor in one command
idf.py -p auto flash monitor
```

#### 5.4. Cấu hình WiFi qua Bluetooth

1. **Mở Android app** → Vào "Add Device"
2. **Scan Bluetooth** → Chọn `SmartBin_XXXXXX`
3. **Nhập WiFi credentials**:
   - SSID: Tên WiFi (2.4GHz)
   - Password: Mật khẩu WiFi
4. **Nhập Device Code** (lấy từ Backend API hoặc Web App)
5. Device tự động kết nối WiFi và đăng ký với server

**Chi tiết**: Xem [esp32-cam/README.md](esp32-cam/README.md)

---

## 📖 Documentation

### 📚 Hướng dẫn chi tiết từng module

| Module | README | Description |
|--------|--------|-------------|
| **AI Model** | [model-fastapi/README.md](model-fastapi/README.md) | Cài đặt FastAPI, train model, test inference |
| **Backend** | [backend-springboot/README.md](backend-springboot/README.md) | Spring Boot setup, API endpoints, database schema |
| **Frontend** | [frontend-nextjs/README.md](frontend-nextjs/README.md) | Next.js setup, routing, components, styling |
| **Mobile** | [android-app/README.md](android-app/README.md) | Android Studio, build APK, Bluetooth setup |
| **Hardware** | [esp32-cam/README.md](esp32-cam/README.md) | ESP-IDF installation, flashing, pin configuration |

### 🔑 Keycloak Admin

- **Admin Console**: http://localhost:8080/admin
  - Manage users, roles, clients
  - Configure authentication flows
  - View audit logs

### 📊 Database Management

**MariaDB** (via command line):

```bash
# Connect to database
mysql -h localhost -P 3333 -u smartbin -p smart_bin_db

# Common queries
SHOW TABLES;
SELECT * FROM device;
SELECT * FROM device_data;
SELECT * FROM classification_logs;
```

**MinIO Console**:

- URL: http://localhost:9001
- Login: minioadmin / minioadmin
- Browse uploaded images
- Manage buckets

---

## 🔧 Troubleshooting

### ❌ Docker issues

#### **Problem: Containers không start được**

```bash
# Check logs
docker-compose logs backend
docker-compose logs model
docker-compose logs keycloak

# Common issues:
# - Port already in use → Change port in docker-compose.yml
# - Database not ready → Wait longer or add healthcheck
# - Out of memory → Increase Docker memory limit
```

#### **Problem: Cannot connect to database**

```bash
# Check if database container is running
docker-compose ps mariadb

# Check database logs
docker-compose logs mariadb

# Test connection from host
mysql -h 127.0.0.1 -P 3333 -u smartbin -p

# Restart database
docker-compose restart mariadb
```

#### **Problem: Permission denied errors**

```bash
# Linux/macOS - fix volume permissions
sudo chown -R $USER:$USER ./docker/mariadb_data
sudo chown -R $USER:$USER ./docker/postgres_data
sudo chown -R $USER:$USER ./docker/minio_data
```

---

### ❌ Backend issues

#### **Problem: Keycloak authentication fails**

```
Error: Unable to connect to Keycloak
```

**Solution:**

1. Kiểm tra Keycloak running: http://localhost:8080
2. Verify realm `smart-bin-realm` exists
3. Check client `smart-bin-client` configuration
4. Copy client secret và cập nhật vào `application.properties`:

```properties
spring.security.oauth2.client.registration.keycloak.client-secret=YOUR_CLIENT_SECRET
```

#### **Problem: Database connection error**

```
Error: Could not connect to MariaDB
```

**Solution:**

```bash
# Check MariaDB is running
docker-compose ps mariadb

# Test connection
mysql -h 127.0.0.1 -P 3333 -u smartbin -p

# Check application.properties
spring.datasource.url=jdbc:mariadb://localhost:3333/smart_bin_db
spring.datasource.username=smartbin
spring.datasource.password=smartbin123
```

#### **Problem: Email verification not working**

**Solution:**

Cấu hình SMTP trong `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

**Note**: Với Gmail, cần tạo [App Password](https://myaccount.google.com/apppasswords)

---

### ❌ Frontend issues

#### **Problem: API requests fail (CORS error)**

```
Access to XMLHttpRequest has been blocked by CORS policy
```

**Solution:**

1. Check Backend CORS configuration:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOrigin("http://localhost:3000");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
```

2. Verify `.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8888
```

#### **Problem: Cannot login (redirect loop)**

**Solution:**

1. Clear browser cache và cookies
2. Check Keycloak redirect URIs:
   - Keycloak Admin → Clients → `smart-bin-client`
   - Valid Redirect URIs: `http://localhost:3000/*`
   - Web Origins: `http://localhost:3000`

---

### ❌ Mobile App issues

#### **Problem: Cannot connect to server**

**Solution:**

1. **Không dùng `localhost`** - ESP32 và Android không thể truy cập localhost của máy tính
2. **Sử dụng IP address của máy**:

```bash
# Windows - Get IP
ipconfig
# Look for "IPv4 Address" (e.g., 192.168.1.100)

# Linux/macOS - Get IP
ifconfig
# Look for "inet" under your network interface (e.g., 192.168.1.100)
```

3. Cập nhật `Constants.java`:

```java
public static final String BASE_URL = "http://192.168.1.100:8888";
```

4. Đảm bảo **phone và server cùng WiFi network**
5. Tắt **firewall** tạm thời (Windows Defender, antivirus)
6. Rebuild và reinstall app

#### **Problem: Bluetooth không tìm thấy ESP32**

**Solution:**

1. Check Bluetooth permissions trong `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

2. Enable Location services trên phone (required for BLE scan)
3. Reset ESP32 và retry
4. Check serial monitor:

```bash
idf.py -p COM3 monitor

# Expected: "Bluetooth LE advertising started: SmartBin_XXXXXX"
```

---

### ❌ ESP32-CAM issues

#### **Problem: ESP32 không kết nối WiFi**

```
[ERROR] WiFi connection failed
```

**Solution:**

1. **WiFi phải là 2.4GHz** (ESP32 không hỗ trợ 5GHz)
2. Check SSID và password đúng (case-sensitive)
3. Reset device: Long press reset button 3 seconds
4. Check WiFi credentials qua serial monitor:

```bash
idf.py -p COM3 monitor

# Look for: "Connecting to WiFi: SSID=YourWiFi"
```

5. Reconfigure WiFi qua Bluetooth:
   - Open Android app → Add Device
   - Re-enter WiFi credentials

#### **Problem: Camera không hoạt động**

```
[ERROR] Camera initialization failed
```

**Solution:**

1. Check camera module kết nối chắc chắn
2. Verify trong `menuconfig`:

```bash
idf.py menuconfig
# → Component config → ESP32-specific → Support for external, SPI-connected RAM
```

3. Test camera với minimal code:

```c
camera_config_t config = {
    .pin_d0 = Y2_GPIO_NUM,
    // ... other pins
    .frame_size = FRAMESIZE_VGA,
    .jpeg_quality = 12,
};

esp_err_t err = esp_camera_init(&config);
if (err != ESP_OK) {
    ESP_LOGE(TAG, "Camera init failed: 0x%x", err);
}
```

4. Power supply: Đảm bảo nguồn 5V/2A đủ mạnh (camera tiêu thụ nhiều điện)

#### **Problem: Servo không chuyển động**

**Solution:**

1. Check power supply:
   - Servos cần nguồn riêng 5V/2A (không dùng chung ESP32)
   - Common ground giữa ESP32 và servo power
2. Check GPIO pins:
   - Servo 1: GPIO 14
   - Servo 2: GPIO 15
   - Servo 3: GPIO 2
3. Test servo code:

```c
// Set servo to 90 degrees
mcpwm_set_duty_in_us(MCPWM_UNIT_0, MCPWM_TIMER_0, MCPWM_OPR_A, 1500);
```

#### **Problem: Ultrasonic sensor không đọc được**

**Solution:**

1. Check wiring:
   - Trigger → GPIO 13
   - Echo → GPIO 12
   - VCC → 5V
   - GND → GND
2. Test với simple code:

```c
// Trigger pulse
gpio_set_level(TRIG_PIN, 1);
esp_rom_delay_us(10);
gpio_set_level(TRIG_PIN, 0);

// Measure echo time
// Calculate distance = (time * 0.034) / 2
```

3. Đảm bảo không có vật cản trước sensor
4. Distance range: 2cm - 400cm

#### **Problem: Device không đăng ký được với server**

```
[ERROR] Failed to register device
```

**Solution:**

1. Check network connectivity:

```bash
# Ping từ ESP32 (add to code)
esp_ping_start();
```

2. Verify Backend API running:

```bash
curl http://192.168.1.100:8888/api/device/health
```

3. Check device code valid:
   - Device code phải được tạo từ Backend trước
   - Get từ Web App → Devices → Add Device
4. Serial monitor để xem lỗi chi tiết:

```bash
idf.py monitor

# Look for HTTP response codes:
# - 200: Success
# - 400: Bad request (invalid code)
# - 401: Unauthorized
# - 500: Server error
```

---

### ❌ Model/AI issues

#### **Problem: Model inference chậm**

**Solution:**

1. Check FastAPI logs:

```bash
docker-compose logs model -f
```

2. Optimize image size từ ESP32:
   - Giảm resolution: `FRAMESIZE_QVGA` (320x240) thay vì `FRAMESIZE_VGA` (640x480)
   - Tăng JPEG compression: `jpeg_quality = 15` (thay vì 10)

3. Nâng cấp lên GPU inference (nếu có GPU):

```python
# In model.py
import onnxruntime as ort
session = ort.InferenceSession(
    "best.onnx",
    providers=['CUDAExecutionProvider', 'CPUExecutionProvider']
)
```

4. Sử dụng lighter model:
   - YOLOv11n-cls (nano) - fastest
   - YOLOv11s-cls (small) - balanced
   - YOLOv11m-cls (medium) - more accurate but slower

#### **Problem: Model classification incorrect**

**Solution:**

1. Check confidence threshold:

```python
# In server.py
if confidence < 0.85:
    return {"error": "Low confidence"}
```

2. Retrain model với more data:
   - Collect more training images
   - Augment data (rotation, brightness, etc.)
   - Balance classes (equal samples per class)

3. Adjust image preprocessing:

```python
# Normalize to [0, 1]
img_array = img_array.astype(np.float32) / 255.0
```

---

### 💡 Đề xuất features

Có ý tưởng feature mới? Tạo issue với:

- **Title**: Tên feature
- **Description**: Mô tả chi tiết feature
- **Use case**: Tại sao feature này cần thiết
- **Mockups**: Design/UI mockups (nếu có)

### 🎯 Areas to contribute

- **Frontend**: Improve UI/UX, add new pages, responsive design
- **Backend**: Optimize APIs, add new features, improve performance
- **Mobile**: Add features, improve UX, fix bugs
- **Hardware**: Optimize firmware, add sensors, improve power management
- **AI Model**: Improve accuracy, add new classes, optimize inference
- **Documentation**: Write tutorials, translate docs, add examples
- **Testing**: Write unit tests, integration tests, E2E tests

---

## 📄 License

MIT License

Copyright (c) 2024-2026 Smart Bin System Team - HUST SoICT

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

## 👥 Contributors

### Development Team

**HUST - School of Information and Communication Technology (SoICT)**
**Project 3 - Smart Bin System**

- **Team Lead**: [Tên của bạn]
- **Backend Developer**: [Tên team member]
- **Frontend Developer**: [Tên team member]
- **Mobile Developer**: [Tên team member]
- **Hardware Engineer**: [Tên team member]
- **AI/ML Engineer**: [Tên team member]

### Supervisor

- **Advisor**: [Tên giảng viên hướng dẫn]
- **Co-Advisor**: [Tên giảng viên phụ] (nếu có)

### Academic Year

**2024 - 2026**

---

## 📧 Contact & Support

### Repository

- **GitHub**: https://github.com/baokieuv/smart-trash-can-system
- **Issues**: https://github.com/baokieuv/smart-trash-can-system/issues
- **Pull Requests**: https://github.com/baokieuv/smart-trash-can-system/pulls

### Social Media

- **Facebook**: [Your Facebook Page]
- **LinkedIn**: [Your LinkedIn]
- **YouTube**: [Demo Videos]

### Email

- **Team Email**: smart-bin-team@gmail.com
- **Support**: support@smartbin.com

---

**🚀 Happy Coding! Hãy cùng nhau bảo vệ môi trường! 🌍♻️**

---

*Last Updated: January 21, 2026*
*Version: 1.0.0*
