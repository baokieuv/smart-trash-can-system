# 🗑️ Smart Bin System

> Hệ thống phân loại rác thông minh sử dụng AI (YOLOv11n-cls) để tự động nhận diện và phân loại rác thải

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-14.x-black.svg)](https://nextjs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-0.100+-009688.svg)](https://fastapi.tiangolo.com/)
[![Android](https://img.shields.io/badge/Android-Java-green.svg)](https://developer.android.com/)

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Demo & Screenshots](#-demo--screenshots)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Tính năng](#-tính-năng)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [API Documentation](#-api-documentation)
- [Authentication & Authorization](#-authentication--authorization)
- [Hardware Flow](#-hardware-flow)
- [Cài đặt](#-cài-đặt)
- [Biến môi trường](#-biến-môi-trường)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Deployment](#-deployment)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Giới thiệu

**Smart Bin System** là một giải pháp IoT toàn diện giúp tự động hóa việc phân loại rác thải thông minh sử dụng Deep Learning. Hệ thống nhận diện 10 loại rác khác nhau và tự động phân loại vào 3 nhóm chính:

### 🔹 Nhóm phân loại

| Nhóm | Loại rác | Mô tả |
|------|----------|-------|
| **♻️ Recyclable (Tái chế)** | `cardboard`, `paper`, `plastic` | Có thể tái chế |
| **🌱 Organic (Hữu cơ)** | `clothes`, `shoes`, `foods` | Phân hủy sinh học |
| **🚫 Non-recyclable** | `battery`, `trash`, `metal`, `glass` | Không tái chế được |

### 🎯 Đối tượng sử dụng

- Người dùng phổ thông mọi lứa tuổi
- Trường học, văn phòng, khu dân cư
- Không dành cho trẻ em (yêu cầu giám sát)

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        SMART BIN SYSTEM                          │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│              │          │              │          │              │
│   Next.js    │◄────────►│  Spring Boot │◄────────►│   FastAPI    │
│  (Web App)   │  REST API│   Backend    │  HTTP    │  (AI Model)  │
│              │          │              │          │              │
└──────────────┘          └──────┬───────┘          └──────────────┘
                                 │                          │
                                 │                          │
                          ┌──────▼───────┐          ┌──────▼───────┐
                          │              │          │              │
                          │   MariaDB    │          │ YOLOv11n-cls │
                          │  (Database)  │          │ ONNX Model   │
                          │              │          │              │
                          └──────────────┘          └──────────────┘
                                 ▲
                                 │
                    ┌────────────┴────────────┐
                    │                         │
            ┌───────▼────────┐       ┌───────▼────────┐
            │                │       │                │
            │  Android App   │       │   ESP32-CAM    │
            │   (Mobile)     │       │  + Servo Motor │
            │                │       │                │
            └────────────────┘       └────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                      KEYCLOAK (Auth Server)                      │
└─────────────────────────────────────────────────────────────────┘
```

### Luồng hoạt động chính:

1. **Web/Mobile**: User upload ảnh → Spring Boot → FastAPI → Nhận diện → Lưu DB
2. **ESP32-CAM**: Chụp ảnh → Gửi server → Nhận kết quả → Điều khiển servo mở nắp thùng tương ứng
3. **Authentication**: Tất cả request đều được xác thực qua Keycloak (OAuth2/JWT)

---

## 📸 Demo & Screenshots

> TODO: Thêm ảnh/video demo

```
[Hình ảnh giao diện Web]
[Hình ảnh giao diện Mobile]
[Video demo ESP32-CAM]
[Sơ đồ phần cứng]
```

---

## 💻 Yêu cầu hệ thống

### Development Environment

| Component | Requirement |
|-----------|-------------|
| **OS** | Windows 10+, macOS 12+, Ubuntu 20.04+ |
| **RAM** | 8GB minimum (16GB recommended) |
| **Storage** | 10GB free space |
| **CPU** | 4 cores recommended |
| **GPU** | Optional (tăng tốc inference) |

### Software Dependencies

| Software | Version | Purpose |
|----------|---------|---------|
| **Java** | 17+ | Spring Boot backend |
| **Node.js** | 18.x+ | Next.js frontend |
| **Python** | 3.9+ | FastAPI model server |
| **Docker** | 20.10+ | Containerization |
| **MariaDB** | 10.6+ | Database |
| **Keycloak** | 23.x+ | Authentication |

### Hardware (cho ESP32-CAM)

- ESP32-CAM module
- Servo motor SG90 (x3)
- Nguồn 5V/2A
- Breadboard & jumper wires

---

## 🚀 Công nghệ sử dụng

### Frontend

| Technology | Description |
|------------|-------------|
| **Next.js 14** | React framework với App Router |
| **TypeScript** | Type-safe development |
| **Tailwind CSS** | Utility-first CSS framework |
| **shadcn/ui** | UI component library |
| **Axios** | HTTP client |

### Mobile

| Technology | Description |
|------------|-------------|
| **Android SDK** | API Level 24+ (Android 7.0+) |
| **Java** | Programming language |
| **XML** | UI layouts |
| **Material Design** | UI/UX guidelines |
| **Retrofit** | HTTP client (nếu dùng) |

### Backend

| Technology | Description |
|------------|-------------|
| **Spring Boot 3.x** | Main API server |
| **Spring Security** | OAuth2 Resource Server |
| **Spring Data JPA** | Database ORM |
| **FastAPI** | Python API cho AI model |
| **Keycloak** | Identity & Access Management |

### AI/ML

| Technology | Description |
|------------|-------------|
| **YOLOv11n-cls** | Classification model |
| **ONNX Runtime** | Model inference engine |
| **Ultralytics** | YOLO framework |

### Database & Cache

| Technology | Description |
|------------|-------------|
| **MariaDB** | Relational database |
| **Redis** | Cache & session storage (nếu dùng) |

### DevOps

| Technology | Description |
|------------|-------------|
| **Docker** | Container platform |
| **Docker Compose** | Multi-container orchestration |
| **GitHub Actions** | CI/CD (TODO) |

---

## ✨ Tính năng

### 🌐 Web Application (Next.js)

- ✅ Upload ảnh để phân loại rác
- ✅ Xem lịch sử phân loại
- ✅ Dashboard thống kê theo thời gian
- ✅ Quản lý thiết bị (devices)
- ✅ Xem thông báo (notifications)
- ✅ Đăng ký, đăng nhập, xác thực email
- ✅ Đổi mật khẩu
- ✅ Responsive design

### 📱 Mobile Application (Android)

- ✅ Đăng ký, đăng nhập với Keycloak
- ✅ Xem danh sách thiết bị
- ✅ Thêm thiết bị mới qua Bluetooth
- ✅ Cấu hình WiFi cho ESP32-CAM
- ✅ Xem chi tiết thiết bị (fill level, battery, waste stats)
- ✅ Xem thông báo real-time
- ✅ Đổi mật khẩu
- ✅ Dark/Light mode
- ✅ Auto-refresh data

### 🤖 AI Model Service (FastAPI)

- ✅ Nhận diện 10 loại rác
- ✅ Trả về label, confidence, group
- ✅ Inference nhanh với ONNX
- ✅ RESTful API endpoint
- ✅ Image preprocessing

### 🔧 Backend Service (Spring Boot)

- ✅ Quản lý user (Keycloak integration)
- ✅ Quản lý devices
- ✅ Lưu trữ device data (fill level, waste counts)
- ✅ Tạo notifications
- ✅ Gọi FastAPI để classify image
- ✅ OAuth2 JWT authentication
- ✅ Email verification
- ✅ Password management

### 🎛️ Hardware (ESP32-CAM)

- ✅ Chụp ảnh rác thải
- ✅ Gửi ảnh lên server qua WiFi
- ✅ Nhận kết quả phân loại
- ✅ Điều khiển 3 servo motor (recyclable, organic, non-recyclable)
- ✅ Gửi status về server
- ✅ Bluetooth configuration

---

## 📂 Cấu trúc dự án

```
smart-bin-system/
│
├── model-fastapi/              # AI Model Service (Python + FastAPI)
│   ├── app/
│   │   ├── main.py            # FastAPI entry point
│   │   ├── model.py           # ONNX model loader
│   │   └── utils.py           # Image preprocessing
│   ├── models/
│   │   └── yolov11n-cls.onnx # Trained model
│   ├── requirements.txt
│   ├── Dockerfile
│   └── README.md
│
├── backend-springboot/         # Main Backend API (Java + Spring Boot)
│   ├── src/main/java/
│   │   └── com/example/smart_bin_server/
│   │       ├── config/        # Security, Keycloak, Redis config
│   │       ├── controller/    # REST controllers
│   │       ├── service/       # Business logic
│   │       ├── repository/    # JPA repositories
│   │       ├── model/         # Entity models
│   │       └── dto/           # Data transfer objects
│   ├── src/main/resources/
│   │   └── application.properties
│   ├── pom.xml
│   ├── Dockerfile
│   └── README.md
│
├── frontend-nextjs/            # Web Frontend (Next.js + TypeScript)
│   ├── src/
│   │   ├── app/               # App Router pages
│   │   ├── components/        # React components
│   │   ├── lib/               # Utilities & helpers
│   │   └── types/             # TypeScript types
│   ├── public/
│   ├── package.json
│   ├── Dockerfile
│   └── README.md
│
├── android-app/                # Mobile App (Android + Java)
│   ├── app/src/main/java/
│   │   └── com/example/smart_bin/
│   │       ├── api/           # API services
│   │       ├── model/         # Data models
│   │       ├── fragments/     # UI fragments
│   │       ├── adapter/       # RecyclerView adapters
│   │       ├── utils/         # Utilities
│   │       └── bluetooth/     # BLE manager
│   ├── app/src/main/res/      # Resources (layouts, drawables)
│   ├── build.gradle
│   └── README.md
│
├── esp32-cam/                  # Firmware cho ESP32-CAM (Arduino C++)
│   ├── smart_bin_esp32/
│   │   ├── smart_bin_esp32.ino
│   │   ├── config.h           # WiFi & server config
│   │   └── servo_control.h    # Servo control logic
│   └── README.md
│
├── docker-compose.yml          # Multi-container setup
├── .env.example               # Environment variables template
├── .gitignore
└── README.md                  # This file
```

### Mô tả các thư mục chính

| Thư mục | Mô tả | Port |
|---------|-------|------|
| `model-fastapi/` | Service AI inference với YOLOv11n-cls ONNX | 8000 |
| `backend-springboot/` | API server chính, kết nối DB, Keycloak, FastAPI | 8080 |
| `frontend-nextjs/` | Web application cho user | 3000 |
| `android-app/` | Mobile app cho Android | N/A |
| `esp32-cam/` | Firmware cho hardware ESP32-CAM | N/A |

---

## 📡 API Documentation

### Base URLs

| Service | URL | Description |
|---------|-----|-------------|
| Spring Boot | `http://localhost:8080` | Main API |
| FastAPI | `http://localhost:8000` | AI Model API |
| Keycloak | `http://localhost:8081` | Auth Server |

---

### 🔐 Authentication APIs

#### 1. Đăng ký người dùng

```http
POST /api/v1/auth/register
```

**Authentication:** Public

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response (200 OK):**
```json
{
  "message": "Registration successful. Please check your email to verify your account.",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": false,
    "createdAt": 1704067200000
  }
}
```

**Error Response (400):**
```json
{
  "error": "Email already registered"
}
```

---

#### 2. Đăng nhập

```http
POST /api/v1/auth/login
```

**Authentication:** Public

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "refresh_token_string",
  "expiresIn": 300,
  "tokenType": "Bearer",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": true,
    "createdAt": 1704067200000
  }
}
```

**Error Response (400):**
```json
{
  "error": "Email not verified. Please check your email for verification link."
}
```

---

#### 3. Refresh Token

```http
POST /api/v1/auth/refresh
```

**Authentication:** Public

**Request Body:**
```json
{
  "refreshToken": "refresh_token_string"
}
```

**Response (200 OK):**
```json
{
  "accessToken": "new_access_token",
  "refreshToken": "new_refresh_token",
  "expiresIn": 300,
  "tokenType": "Bearer",
  "user": null
}
```

---

#### 4. Đăng xuất

```http
POST /api/v1/auth/logout
```

**Authentication:** Bearer Token

**Request Headers:**
```
Authorization: Bearer {accessToken}
```

**Request Body:**
```json
{
  "refreshToken": "refresh_token_string"
}
```

**Response (200 OK):**
```json
{
  "message": "Logged out successfully"
}
```

---

#### 5. Đổi mật khẩu

```http
POST /api/v1/auth/change-password
```

**Authentication:** Bearer Token

**Request Body:**
```json
{
  "currentPassword": "oldpass123",
  "newPassword": "newpass456",
  "confirmPassword": "newpass456"
}
```

**Response (200 OK):**
```json
{
  "message": "Password changed successfully. Please login again with your new password."
}
```

---

#### 6. Xác thực email

```http
GET /api/v1/auth/verify-email?token={verificationToken}
```

**Authentication:** Public

**Response (200 OK):**
```json
{
  "message": "Email verified successfully"
}
```

---

#### 7. Gửi lại email xác thực

```http
POST /api/v1/auth/resend-verification
```

**Authentication:** Public

**Request Body:**
```json
{
  "email": "user@example.com"
}
```

**Response (200 OK):**
```json
{
  "message": "Verification email sent successfully"
}
```

---

#### 8. Lấy thông tin user hiện tại

```http
GET /api/v1/auth/me
```

**Authentication:** Bearer Token

**Response (200 OK):**
```json
{
  "id": "uuid",
  "email": "user@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "emailVerified": true,
  "createdAt": 1704067200000
}
```

---

### 🎛️ Device Management APIs

#### 1. Tạo device mới

```http
POST /api/v1/devices
```

**Authentication:** Bearer Token

**Request Body:**
```json
{
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "name": "Smart Bin 01"
}
```

**Response (200 OK):**
```json
{
  "id": "AA_BB_CC_DD_EE_FF",
  "name": "Smart Bin 01",
  "status": "OFFLINE",
  "userId": "user_uuid",
  "createdAt": 1704067200000,
  "updatedAt": 1704067200000
}
```

---

#### 2. Lấy danh sách devices

```http
GET /api/v1/devices
```

**Authentication:** Bearer Token

**Response (200 OK):**
```json
[
  {
    "id": "AA_BB_CC_DD_EE_FF",
    "name": "Smart Bin 01",
    "status": "ONLINE",
    "userId": "user_uuid",
    "createdAt": 1704067200000,
    "updatedAt": 1704067300000
  }
]
```

---

#### 3. Lấy thông tin device theo ID

```http
GET /api/v1/devices/{deviceId}
```

**Authentication:** Bearer Token

**Response (200 OK):**
```json
{
  "id": "AA_BB_CC_DD_EE_FF",
  "name": "Smart Bin 01",
  "status": "ONLINE"
}
```

---

#### 4. Cập nhật device

```http
PUT /api/v1/devices/{deviceId}
```

**Authentication:** Bearer Token

**Request Body:**
```json
{
  "name": "Smart Bin Living Room",
  "status": "ONLINE"
}
```

**Response (200 OK):**
```json
{
  "id": "AA_BB_CC_DD_EE_FF",
  "name": "Smart Bin Living Room",
  "status": "ONLINE"
}
```

---

#### 5. Xóa device

```http
DELETE /api/v1/devices/{deviceId}
```

**Authentication:** Bearer Token

**Response (200 OK):**
```json
"AA_BB_CC_DD_EE_FF"
```

---

### 📊 Device Data APIs

#### 1. Gửi dữ liệu từ ESP32-CAM

```http
POST /api/v1/devices/{deviceId}/data
```

**Authentication:** Public (hoặc API Key cho ESP32)

**Request Body:**
```json
{
  "recycledWasteCount": 5,
  "nonRecycledWasteCount": 3,
  "compostableWasteCount": 2,
  "fillLevel": 45,
  "isFull": false
}
```

**Response (200 OK):**
```json
{
  "deviceId": "AA_BB_CC_DD_EE_FF",
  "statusCode": 200,
  "message": "Successfully",
  "timestamp": 1704067200000
}
```

---

#### 2. Lấy dữ liệu device

```http
GET /api/v1/devices/{deviceId}/data
```

**Authentication:** Bearer Token

**Response (200 OK):**
```json
{
  "deviceId": "AA_BB_CC_DD_EE_FF",
  "recycledWasteCount": 5,
  "nonRecycledWasteCount": 3,
  "compostableWasteCount": 2,
  "fillLevel": 45,
  "isFull": false,
  "timestamp": 1704067200000
}
```

---

### 🔔 Notification APIs

#### 1. Lấy danh sách thông báo

```http
GET /api/v1/notifications
```

**Authentication:** Bearer Token

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "deviceId": "AA_BB_CC_DD_EE_FF",
    "deviceName": "Smart Bin 01",
    "message": "Device connected successfully",
    "type": "SUCCESS",
    "timestamp": 1704067200000
  },
  {
    "id": 2,
    "deviceId": "AA_BB_CC_DD_EE_FF",
    "deviceName": "Smart Bin 01",
    "message": "Bin is 80% full",
    "type": "WARNING",
    "timestamp": 1704067300000
  }
]
```

---

### 🤖 AI Classification API

#### 1. Phân loại rác từ ảnh

```http
POST /api/v1/classify-image
```

**Authentication:** Public

**Request:**
- Content-Type: `multipart/form-data`
- Body: `image` (file)

**Response (200 OK):**
```json
{
  "label": "plastic",
  "confidence": 0.95,
  "category": "recyclable"
}
```

**Hoặc gọi trực tiếp FastAPI:**

```http
POST http://localhost:8000/classify
```

**Request:**
- Content-Type: `multipart/form-data`
- Body: `image` (file)

**Response (200 OK):**
```json
{
  "label": "cardboard",
  "confidence": 0.92,
  "category": "recyclable"
}
```

---

### 🔄 Internal API (Spring Boot ↔ FastAPI)

Spring Boot gọi FastAPI để inference:

```http
POST http://fastapi-service:8000/classify
Content-Type: multipart/form-data

image: [binary data]
```

FastAPI trả về:
```json
{
  "label": "paper",
  "confidence": 0.88,
  "category": "recyclable"
}
```

---

## 🔐 Authentication & Authorization

Hệ thống sử dụng **Keycloak** làm Identity Provider với cơ chế **OAuth2 + JWT**.

### Flow đăng ký & đăng nhập

```
1. User đăng ký → Spring Boot tạo user trong Keycloak (disabled)
2. User nhận email verification → Click link xác thực
3. Spring Boot enable user trong Keycloak
4. User đăng nhập → Keycloak cấp Access Token (JWT) + Refresh Token
5. Client lưu token → Gửi kèm request qua header: Authorization: Bearer {token}
6. Spring Boot verify JWT → Cho phép truy cập resource
```

### Token Management

- **Access Token**: Hết hạn sau 5 phút (300s)
- **Refresh Token**: Hết hạn sau 2 giờ (7200s)
- **Token Refresh**: Client tự động gọi `/api/v1/auth/refresh` khi token hết hạn
- **Logout**: Revoke refresh token tại Keycloak

### Keycloak Configuration

```yaml
Realm: smart-bin-realm
Client ID: smart-bin-client
Client Secret: [generated by Keycloak]
Grant Type: password, refresh_token
```

### Security cho API

| Endpoint | Auth Required |
|----------|---------------|
| `/api/v1/auth/**` | ❌ Public |
| `/api/v1/classify-image/**` | ❌ Public |
| `/api/v1/devices/**` | ✅ Bearer Token |
| `/api/v1/notifications/**` | ✅ Bearer Token |

---

## 🎛️ Hardware Flow

### Luồng hoạt động ESP32-CAM

```
┌─────────────────────────────────────────────────────────────┐
│  1. ESP32-CAM khởi động, kết nối WiFi                       │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  2. Phát hiện rác (trigger sensor hoặc button)              │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  3. Chụp ảnh bằng camera OV2640                             │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Gửi ảnh lên server qua HTTP POST                        │
│     → POST /api/v1/classify-image                           │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  5. Server phân loại rác (FastAPI + YOLO)                   │
│     → Trả về: label, confidence, category                   │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  6. ESP32 nhận kết quả, điều khiển servo motor              │
│     • recyclable    → Servo 1 (90°)                         │
│     • organic       → Servo 2 (90°)                         │
│     • non-recyclable → Servo 3 (90°)                        │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  7. Gửi device data về server                               │
│     → POST /api/v1/devices/{deviceId}/data                  │
│     (update waste count, fill level)                        │
└─────────────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────────────┐
│  8. Server lưu DB, gửi notification (nếu cần)               │
└─────────────────────────────────────────────────────────────┘
```

### Kết nối phần cứng

```
ESP32-CAM Pinout:
├── GPIO 12 → Servo 1 (Recyclable)
├── GPIO 13 → Servo 2 (Organic)
├── GPIO 15 → Servo 3 (Non-recyclable)
├── 5V      → Power supply
└── GND     → Ground
```

---

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/your-username/smart-bin-system.git
cd smart-bin-system
```

### 2. Cài đặt dependencies cho từng module

#### 2.1. FastAPI (AI Model)

```bash
cd model-fastapi
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
```

#### 2.2. Spring Boot (Backend)

```bash
cd backend-springboot
./mvnw clean install  # Windows: mvnw.cmd clean install
```

#### 2.3. Next.js (Frontend)

```bash
cd frontend-nextjs
npm install
# hoặc
yarn install
```

#### 2.4. Android App

- Mở project trong Android Studio
- Sync Gradle
- Build project

#### 2.5. ESP32-CAM

- Cài Arduino IDE hoặc PlatformIO
- Cài thư viện: `ESP32`, `WiFi`, `HTTPClient`, `Servo`
- Upload code lên ESP32-CAM

---

## ⚙️ Biến môi trường

### Backend (Spring Boot)

Tạo file `backend-springboot/src/main/resources/application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mariadb://localhost:3306/smart_bin_db
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update

# Keycloak
keycloak.server-url=http://localhost:8081
keycloak.realm=smart-bin-realm
keycloak.client-id=smart-bin-client
keycloak.client-secret=your_keycloak_secret
keycloak.admin-username=admin
keycloak.admin-password=admin

spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/smart-bin-realm

# Redis (nếu dùng)
spring.redis.host=localhost
spring.redis.port=6379

# Email
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

app.email.from=noreply@smartbin.com
app.email.verification.url=http://localhost:3000/verify-email

# FastAPI URL
app.ai-server.url=http://localhost:8000
```

### Frontend (Next.js)

Tạo file `frontend-nextjs/.env.local`:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_KEYCLOAK_URL=http://localhost:8081
NEXT_PUBLIC_KEYCLOAK_REALM=smart-bin-realm
NEXT_PUBLIC_KEYCLOAK_CLIENT_ID=smart-bin-client
```

### Android App

File `android-app/app/src/main/java/com/example/smart_bin/utils/Constants.java`:

```java
public class Constants {
    public static final String BASE_URL = "http://YOUR_SERVER_IP:8080";
    // ... other constants
}
```

### ESP32-CAM

File `esp32-cam/smart_bin_esp32/config.h`:

```cpp
// WiFi credentials
const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";

// Server URL
const char* serverUrl = "http://YOUR_SERVER_IP:8080";
const char* deviceId = "AA_BB_CC_DD_EE_FF";
```

---

## 🚀 Chạy ứng dụng

### Option 1: Chạy thủ công (Development)

#### 1. Start MariaDB

```bash
# Docker
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=smart_bin_db \
  mariadb:10.6
```

#### 2. Start Keycloak

```bash
docker run -d \
  --name keycloak \
  -p 8081:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:23.0 \
  start-dev
```

**Cấu hình Keycloak:**
- Truy cập: http://localhost:8081
- Login: admin/admin
- Tạo realm: `smart-bin-realm`
- Tạo client: `smart-bin-client`
- Enable service accounts & authorization
- Lấy client secret

#### 3. Start FastAPI

```bash
cd model-fastapi
source venv/bin/activate
uvicorn app.main:app --reload --port 8000
```

#### 4. Start Spring Boot

```bash
cd backend-springboot
./mvnw spring-boot:run
```

#### 5. Start Next.js

```bash
cd frontend-nextjs
npm run dev
```

#### 6. Build & Install Android App

- Mở Android Studio
- Build → Make Project
- Run app trên emulator hoặc device

#### 7. Upload code lên ESP32-CAM

- Kết nối ESP32-CAM qua FTDI
- Upload code từ Arduino IDE
- Monitor Serial để debug

---

### Option 2: Chạy với Docker Compose

#### 1. Tạo file `docker-compose.yml`

```yaml
version: '3.8'

services:
  mariadb:
    image: mariadb:10.6
    container_name: smart-bin-mariadb
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: smart_bin_db
    ports:
      - "3306:3306"
    volumes:
      - mariadb_data:/var/lib/mysql
    networks:
      - smart-bin-network

  keycloak:
    image: quay.io/keycloak/keycloak:23.0
    container_name: smart-bin-keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    ports:
      - "8081:8080"
    command: start-dev
    networks:
      - smart-bin-network

  fastapi:
    build: ./model-fastapi
    container_name: smart-bin-fastapi
    ports:
      - "8000:8000"
    volumes:
      - ./model-fastapi/models:/app/models
    networks:
      - smart-bin-network

  backend:
    build: ./backend-springboot
    container_name: smart-bin-backend
    depends_on:
      - mariadb
      - keycloak
      - fastapi
    environment:
      SPRING_DATASOURCE_URL: jdbc:mariadb://mariadb:3306/smart_bin_db
      KEYCLOAK_SERVER_URL: http://keycloak:8080
      AI_SERVER_URL: http://fastapi:8000
    ports:
      - "8080:8080"
    networks:
      - smart-bin-network

  frontend:
    build: ./frontend-nextjs
    container_name: smart-bin-frontend
    environment:
      NEXT_PUBLIC_API_URL: http://localhost:8080
    ports:
      - "3000:3000"
    depends_on:
      - backend
    networks:
      - smart-bin-network

volumes:
  mariadb_data:

networks:
  smart-bin-network:
    driver: bridge
```

#### 2. Chạy tất cả services

```bash
docker-compose up -d
```

#### 3. Kiểm tra logs

```bash
docker-compose logs -f
```

#### 4. Dừng services

```bash
docker-compose down
```

---

## 🌐 Deployment

### Deploy lên VPS (Ubuntu)

#### 1. Cài đặt Docker & Docker Compose

```bash
sudo apt update
sudo apt install docker.io docker-compose -y
sudo systemctl enable docker
sudo systemctl start docker
```

#### 2. Clone repository

```bash
git clone https://github.com/your-username/smart-bin-system.git
cd smart-bin-system
```

#### 3. Cấu hình environment variables

```bash
cp .env.example .env
nano .env  # Chỉnh sửa các biến môi trường
```

#### 4. Build & Run

```bash
docker-compose up -d --build
```

#### 5. Cấu hình Nginx Reverse Proxy (Optional)

```nginx
# /etc/nginx/sites-available/smartbin
server {
    listen 80;
    server_name your-domain.com;

    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /auth {
        proxy_pass http://localhost:8081;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

#### 6. Enable HTTPS với Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d your-domain.com
```

---

### Deploy trên Cloud (AWS/GCP/Azure)

**TODO**: Hướng dẫn chi tiết deploy lên cloud provider

---

## 🐛 Troubleshooting

### Lỗi thường gặp

#### 1. Không kết nối được MariaDB

**Lỗi:**
```
Communications link failure
```

**Giải pháp:**
```bash
# Kiểm tra MariaDB đang chạy
docker ps | grep mariadb

# Kiểm tra port 3306
netstat -an | grep 3306

# Restart MariaDB
docker restart mariadb
```

---

#### 2. Keycloak không trả về token

**Lỗi:**
```json
{
  "error": "invalid_grant"
}
```

**Giải pháp:**
- Kiểm tra user đã được enable trong Keycloak
- Kiểm tra email đã verified
- Kiểm tra client secret đúng
- Clear browser cache & cookies

---

#### 3. FastAPI không inference được

**Lỗi:**
```
Model not found
```

**Giải pháp:**
```bash
# Kiểm tra model file tồn tại
ls -la model-fastapi/models/yolov11n-cls.onnx

# Test FastAPI endpoint
curl -X POST http://localhost:8000/classify \
  -F "image=@test.jpg"
```

---

#### 4. ESP32-CAM không kết nối WiFi

**Lỗi:**
```
WiFi connection failed
```

**Giải pháp:**
- Kiểm tra SSID & password đúng
- ESP32 chỉ hỗ trợ WiFi 2.4GHz
- Kiểm tra tường lửa router
- Reset ESP32 bằng nút RESET

---

#### 5. Android app crash khi login

**Lỗi:**
```
SecurityException: Permission denied
```

**Giải pháp:**
- Cấp quyền INTERNET trong AndroidManifest.xml
- Cấp quyền BLUETOOTH, LOCATION cho scanning
- Kiểm tra BASE_URL đúng (dùng IP thật, không dùng localhost)

---

### Debug Tips

#### Backend Logs

```bash
# Spring Boot
tail -f logs/spring-boot-app.log

# Docker logs
docker logs -f smart-bin-backend
```

#### FastAPI Logs

```bash
docker logs -f smart-bin-fastapi
```

#### Android Logcat

```bash
adb logcat | grep SmartBin
```

---

## 🤝 Contributing

Chúng tôi hoan nghênh mọi đóng góp! Vui lòng làm theo các bước sau:

### 1. Fork repository

### 2. Tạo branch mới

```bash
git checkout -b feature/amazing-feature
```

### 3. Commit changes

```bash
git commit -m "Add some amazing feature"
```

### 4. Push to branch

```bash
git push origin feature/amazing-feature
```

### 5. Tạo Pull Request

### Coding Standards

- **Java**: Follow Google Java Style Guide
- **Python**: Follow PEP 8
- **JavaScript/TypeScript**: Follow Airbnb Style Guide
- **Android**: Follow Android Kotlin Style Guide

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Authors

- **Your Name** - *Initial work* - [YourGithub](https://github.com/yourusername)

---

## 🙏 Acknowledgments

- YOLOv11 by Ultralytics
- Spring Boot community
- Next.js team
- Keycloak project
- ESP32 community

---

## 📞 Contact & Support

- **Email**: support@smartbin.com
- **Website**: https://smartbin.com
- **Issues**: [GitHub Issues](https://github.com/your-username/smart-bin-system/issues)
- **Discord**: [Join our community](https://discord.gg/smartbin)

---

<div align="center">
  <p>Made with ❤️ by Smart Bin Team</p>
  <p>⭐ Star us on GitHub — it motivates us a lot!</p>
</div>