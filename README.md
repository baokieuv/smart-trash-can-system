# 🗑️ Smart Bin System

> Hệ thống phân loại rác thông minh sử dụng AI (YOLOv11n-cls) để tự động nhận diện và phân loại rác thải

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Next.js](https://img.shields.io/badge/Next.js-16.x-black.svg)](https://nextjs.org/)
[![FastAPI](https://img.shields.io/badge/FastAPI-Latest-009688.svg)](https://fastapi.tiangolo.com/)
[![Android](https://img.shields.io/badge/Android-7.0+-green.svg)](https://developer.android.com/)

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Kiến trúc hệ thống](#-kiến-trúc-hệ-thống)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Tính năng chính](#-tính-năng-chính)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Quick Start](#-quick-start)
- [Documentation](#-documentation)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🎯 Giới thiệu

**Smart Bin System** là giải pháp IoT toàn diện giúp tự động hóa việc phân loại rác thải sử dụng AI. Hệ thống nhận diện 10 loại rác và tự động phân loại vào 3 nhóm chính:

### 🔹 Nhóm phân loại

| Nhóm | Loại rác | Mô tả |
|------|----------|-------|
| **♻️ Recyclable** | `cardboard`, `paper`, `plastic`, `metal`, `glass` | Có thể tái chế |
| **🌱 Compostable** | `biological`, `clothes`, `shoes` | Phân hủy sinh học |
| **🚫 Non-recyclable** | `battery`, `trash` | Không tái chế được |

### 🎯 Đối tượng sử dụng

- Người dùng cá nhân, hộ gia đình
- Trường học, văn phòng, khu công cộng
- Cơ quan, tổ chức muốn phân loại rác tự động

---

## 🏗️ Kiến trúc hệ thống

```
┌─────────────────────────────────────────────────────────────────┐
│                        SMART BIN SYSTEM                         │
└─────────────────────────────────────────────────────────────────┘

┌──────────────┐          ┌──────────────┐          ┌──────────────┐
│   Next.js    │          │   MariaDB    │    ┌────►│   FastAPI    │
│  (Web App)   │          │  (Database)  │    │     │  (AI Model)  │
└──────┬───────┘          └──────┬───────┘    │     └──────┬───────┘
       │                         │            │            │
       │   REST API       ┌──────▼───────┐    │     ┌──────▼───────┐
       └─────────────────►│ Spring Boot  │────┘     │ YOLOv11n-cls │
                          │   Backend    │          │ ONNX Model   │
                          └──────┬───────┘          └──────────────┘
                                 │
                    ┌────────────┴────────────┐
                    │                         │
            ┌───────▼────────┐       ┌───────▼────────┐
            │  Android App   │       │   ESP32-CAM    │
            │   (Mobile)     │       │  + Hardware    │
            └────────────────┘       └────────────────┘

┌─────────────────────────────────────────────────────────────────┐
│                   KEYCLOAK (OAuth2 Server)                       │
└─────────────────────────────────────────────────────────────────┘
```

### Luồng hoạt động:

1. **ESP32-CAM**: Phát hiện rác → Chụp ảnh → Gửi lên Backend
2. **Backend**: Nhận ảnh → Gọi FastAPI → Nhận kết quả phân loại → Lưu DB
3. **ESP32-CAM**: Nhận kết quả → Mở nắp thùng tương ứng (servo motor)
4. **Web/Mobile**: Xem thống kê, quản lý thiết bị, nhận thông báo
5. **Authentication**: Tất cả API bảo mật qua Keycloak OAuth2/JWT

---

## 🚀 Công nghệ sử dụng

### Frontend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Next.js** | 16.0.7 | React framework với App Router |
| **React** | 19.2.0 | UI library |
| **TypeScript** | 5.x | Type-safe development |
| **Tailwind CSS** | 4.x | Styling |

### Mobile

| Technology | Version | Purpose |
|------------|---------|---------|
| **Android SDK** | 24-36 | Platform (Android 7.0-15) |
| **Java** | 11 | Programming language |
| **Material Design** | 3.x | UI components |
| **Gson** | 2.10.1 | JSON parsing |

### Backend

| Technology | Version | Purpose |
|------------|---------|---------|
| **Spring Boot** | 4.0.0 | API server (Java 21) |
| **Spring Security** | Latest | OAuth2 + JWT |
| **Spring Data JPA** | Latest | Database ORM |
| **Keycloak** | 23.0.3 | Identity provider |
| **MariaDB** | 10.6+ | Database |
| **MinIO** | 8.6.0 | Object storage |

### AI/ML

| Technology | Version | Purpose |
|------------|---------|---------|
| **FastAPI** | Latest | API framework |
| **YOLOv11n-cls** | Latest | Classification model |
| **ONNX Runtime** | Latest | Model inference |
| **OpenCV** | Latest | Image processing |

### Hardware

| Component | Model | Purpose |
|-----------|-------|---------|
| **ESP32-S3 CAM** | ESP32-S3 | Controller + Camera |
| **Servo Motor** | SG90 x3 | Lid control |
| **Ultrasonic** | HC-SR04 | Distance sensor |
| **LED & Buzzer** | - | Status indicator |

---

## ✨ Tính năng chính

### 🌐 Web Application (Next.js)

- ✅ Dashboard tổng quan (số thiết bị, thống kê rác)
- ✅ Quản lý thiết bị Smart Bin
- ✅ Xem lịch sử phân loại rác
- ✅ Nhận thông báo (thùng đầy, lỗi thiết bị)
- ✅ Đăng ký, đăng nhập, xác thực email
- ✅ Quản lý tài khoản (đổi mật khẩu)
- ✅ Responsive design (mobile-friendly)

### 📱 Mobile Application (Android)

- ✅ Đăng ký & đăng nhập với Keycloak OAuth2
- ✅ Dashboard thống kê real-time
- ✅ Quản lý danh sách thiết bị
- ✅ **Thêm thiết bị mới qua Bluetooth**
- ✅ **Cấu hình WiFi cho ESP32-CAM qua Bluetooth**
- ✅ Xem chi tiết thiết bị (fill level, battery, waste stats)
- ✅ Nhận thông báo push
- ✅ Dark/Light mode
- ✅ Auto-refresh data (mỗi 30s)

### 🤖 AI Model Service (FastAPI)

- ✅ Nhận diện 10 loại rác (YOLOv11n-cls)
- ✅ Trả về label, confidence score (> 0.85), category
- ✅ Fast inference với ONNX (~70ms/image)
- ✅ RESTful API endpoint
- ✅ Image preprocessing & validation

### 🔧 Backend Service (Spring Boot)

- ✅ User management (tích hợp Keycloak)
- ✅ Device management (CRUD operations)
- ✅ Device data storage (waste counts, fill level)
- ✅ Notification system
- ✅ Image classification (gọi FastAPI)
- ✅ OAuth2 JWT authentication
- ✅ Email verification & password management
- ✅ Scheduled tasks (device status check)

### 🎛️ Hardware (ESP32-CAM)

- ✅ Tự động phát hiện rác (ultrasonic sensor)
- ✅ Chụp ảnh rác thải (camera)
- ✅ Gửi ảnh lên server qua WiFi/HTTP
- ✅ Nhận kết quả phân loại từ server
- ✅ **Điều khiển 3 servo motor** (mở nắp tự động)
- ✅ Đo mức độ đầy của thùng (fill level %)
- ✅ Gửi device status định kỳ (10s)
- ✅ **Bluetooth configuration** (WiFi setup, device pairing)
- ✅ **Deep sleep mode** (tiết kiệm pin)
- ✅ LED & Buzzer alerts

---

## 📂 Cấu trúc dự án

```
smart-bin-system/
│
├── model-fastapi/              # AI Model Service (Python + FastAPI)
│   ├── server.py               # FastAPI entry point
│   ├── model.py                # ONNX inference wrapper
│   ├── best.onnx               # Trained YOLOv11n-cls model
│   ├── requirements.txt        # Python dependencies
│   └── README.md               # 📖 Hướng dẫn cài đặt & chạy
│
├── backend-springboot/         # Backend API (Spring Boot + Java 21)
│   ├── src/main/java/          # Source code
│   ├── src/main/resources/     # Config files
│   ├── pom.xml                 # Maven dependencies
│   └── README.md               # 📖 Hướng dẫn cài đặt & chạy
│
├── frontend-nextjs/            # Web App (Next.js 16 + React 19)
│   ├── app/                    # App Router pages
│   ├── components/             # React components
│   ├── lib/                    # Utilities
│   ├── package.json            # NPM dependencies
│   └── README.md               # 📖 Hướng dẫn cài đặt & chạy
│
├── android-app/                # Mobile App (Android + Java)
│   ├── app/src/main/           # Android source code
│   ├── build.gradle.kts        # Gradle config
│   └── README.md               # 📖 Hướng dẫn cài đặt & build
│
├── esp32-cam/                  # ESP32-CAM Firmware (C + ESP-IDF)
│   ├── main/main.c             # Main application
│   ├── components/             # Hardware modules
│   ├── CMakeLists.txt          # Build config
│   └── README.md               # 📖 Hướng dẫn flash & cấu hình
│
├── docker/                     # Docker services
│   ├── docker-compose.yml      # Multi-container setup
│   └── [data folders]          # Persistent data
│
├── docs/                       # Documentation (Vietnamese)
│   ├── chuong1.md              # Tổng quan hệ thống
│   ├── chuong2.md              # Phân tích & thiết kế
│   ├── chuong3.md              # Triển khai
│   ├── chuong4.md              # Kết quả & đánh giá
│   └── Use_Case_Chi_Tiet.md   # Use cases
│
├── DEPLOYMENT.md               # 🚀 Hướng dẫn deploy production
└── README.md                   # 📄 File này
```

---

## 🚀 Quick Start

### Yêu cầu hệ thống

- **Docker** & **Docker Compose** (recommended)
- **Java 21**, **Node.js 20+**, **Python 3.8+** (nếu chạy native)
- **ESP-IDF v5.0+** (cho ESP32-CAM)
- **Android Studio** (cho mobile app)

### Option 1: Chạy với Docker (Khuyến nghị)

```bash
# Clone repository
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system

# Start all services
cd docker
docker-compose up -d

# Kiểm tra services
docker-compose ps
```

**Services sẽ chạy tại:**
- Web App: http://localhost:3000
- Backend API: http://localhost:8888
- FastAPI: http://localhost:8000
- Keycloak: http://localhost:8080

### Option 2: Chạy từng service riêng

Chi tiết xem trong README của từng module:

1. **[FastAPI Model](model-fastapi/README.md)** - Chạy trước (port 8000)
2. **[Spring Boot Backend](backend-springboot/README.md)** - Chạy sau FastAPI (port 8888)
3. **[Next.js Frontend](frontend-nextjs/README.md)** - Chạy cuối cùng (port 3000)
4. **[Android App](android-app/README.md)** - Build APK và cài đặt
5. **[ESP32-CAM](esp32-cam/README.md)** - Flash firmware và cấu hình

### Thiết lập ban đầu

1. **Khởi tạo Keycloak** (lần đầu)
   - Truy cập http://localhost:8080
   - Login: admin/admin
   - Tạo realm: `smart-bin-realm`
   - Tạo client: `smart-bin-client`

2. **Đăng ký tài khoản**
   - Truy cập http://localhost:3000/register
   - Điền thông tin → Nhận email xác thực
   - Click link xác thực → Đăng nhập

3. **Thêm thiết bị ESP32-CAM**
   - Mở Android App → Add Device → Scan Bluetooth
   - Chọn `SmartBin_XXXXXX` → Nhập WiFi credentials
   - Thiết bị tự động kết nối và đăng ký với server

---

## 📖 Documentation

### Hướng dẫn chi tiết

| Component | README | Description |
|-----------|--------|-------------|
| **AI Model** | [model-fastapi/README.md](model-fastapi/README.md) | Cài đặt Python, chạy FastAPI, test API |
| **Backend** | [backend-springboot/README.md](backend-springboot/README.md) | Cài đặt Java/Maven, config DB, chạy Spring Boot |
| **Frontend** | [frontend-nextjs/README.md](frontend-nextjs/README.md) | Cài đặt Node.js, chạy Next.js dev/prod |
| **Mobile** | [android-app/README.md](android-app/README.md) | Android Studio, build APK, cấu hình |
| **Hardware** | [esp32-cam/README.md](esp32-cam/README.md) | ESP-IDF setup, flash firmware, pin config |
| **Deployment** | [DEPLOYMENT.md](DEPLOYMENT.md) | Deploy production với Docker/Cloud |

### API Documentation

- **Swagger UI**: http://localhost:8888/swagger-ui.html (Spring Boot)
- **FastAPI Docs**: http://localhost:8000/docs (AI Model)
- **Keycloak Admin**: http://localhost:8080/admin

### Báo cáo dự án (Vietnamese)

Tài liệu chi tiết trong thư mục [docs/](docs/):
- [Chương 1: Tổng quan](docs/chuong1.md)
- [Chương 2: Phân tích & Thiết kế](docs/chuong2.md)
- [Chương 3: Triển khai](docs/chuong3.md)
- [Chương 4: Kết quả & Đánh giá](docs/chuong4.md)
- [Use Case Chi tiết](docs/Use_Case_Chi_Tiet.md)

---

## 🔧 Troubleshooting

### Services không start được

```bash
# Kiểm tra logs
docker-compose logs backend
docker-compose logs model

# Restart service
docker-compose restart backend
```

### ESP32 không kết nối WiFi

- Kiểm tra WiFi 2.4GHz (ESP32 không hỗ trợ 5GHz)
- Reset device (long press button 3s)
- Xem serial monitor: `idf.py monitor`

### Android app không kết nối server

- Kiểm tra IP server trong Constants.java
- Đảm bảo phone và server cùng network
- Tắt firewall/antivirus tạm thời

### Model inference chậm

- Kiểm tra FastAPI logs: `docker-compose logs model`
- Nâng cấp lên GPU inference (optional)
- Giảm resolution ảnh từ ESP32

Chi tiết xem README của từng module!

---

## 🤝 Contributing

Chúng tôi hoan nghênh mọi đóng góp! Vui lòng:

1. Fork repository
2. Tạo branch mới: `git checkout -b feature/your-feature`
3. Commit changes: `git commit -m 'Add your feature'`
4. Push to branch: `git push origin feature/your-feature`
5. Tạo Pull Request

---

## 📄 License

MIT License - Xem file [LICENSE](LICENSE) để biết thêm chi tiết.

---

## 👥 Contributors

**HUST - SoICT - Project 3**

- **Development Team**: Smart Bin System
- **Supervisor**: [Tên giảng viên]
- **Year**: 2024-2026

---

## 📧 Contact

- **Repository**: https://github.com/baokieuv/smart-trash-can-system
- **Issues**: https://github.com/baokieuv/smart-trash-can-system/issues
- **Email**: [your-email@example.com]

---

**Happy Coding! 🚀 Hãy chung tay bảo vệ môi trường! 🌍♻️**
