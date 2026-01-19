# 🌐 Smart Bin Backend (Spring Boot)

API server chính của hệ thống Smart Bin, xử lý authentication, device management, và tích hợp với AI model.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ)
- [Yêu cầu](#-yêu cầu)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Cấu trúc code](#-cấu-trúc-code)
- [API Endpoints](#-api-endpoints)
- [Database Schema](#-database-schema)
- [Keycloak Integration](#-keycloak-integration)
- [MinIO Storage](#-minio-storage)
- [Email Service](#-email-service)
- [Scheduled Tasks](#-scheduled-tasks)
- [Chạy ứng dụng](#-chạy-ứng-dụng)

---

## 🎯 Giới thiệu

Spring Boot backend là core API server của Smart Bin System, cung cấp:

- RESTful APIs cho Web & Mobile clients
- Tích hợp Keycloak để authentication/authorization
- Quản lý devices và device data
- Gọi FastAPI để classify images
- Gửi email verification
- Scheduled tasks để check device status

---

## ✨ Tính năng

### 🔐 Authentication & Authorization
- ✅ Đăng ký user với Keycloak
- ✅ Email verification
- ✅ Login với OAuth2 (password grant)
- ✅ Refresh token
- ✅ Logout (revoke token)
- ✅ Change password
- ✅ JWT validation

### 🎛️ Device Management
- ✅ CRUD devices
- ✅ Device ownership validation
- ✅ Device status tracking (ONLINE/OFFLINE)
- ✅ Auto device status check (scheduled task)

### 📊 Device Data
- ✅ Lưu waste counts (recyclable, non-recyclable, compostable)
- ✅ Fill level tracking
- ✅ Battery level (TODO)
- ✅ Timestamp tracking

### 🔔 Notifications
- ✅ System notifications
- ✅ Device event notifications
- ✅ Paginated list

### 🤖 AI Integration
- ✅ Call FastAPI để classify images
- ✅ Multipart file upload
- ✅ Return label, confidence, category

### 📦 Storage (MinIO)
- ✅ Upload images to object storage
- ✅ Generate presigned URLs
- ✅ Image metadata management
- ✅ Bucket management

---

## 🚀 Công nghệ

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 4.0.0 | Framework |
| **Spring Security** | 6.x | OAuth2 Resource Server |
| **Spring Data JPA** | Latest | Database ORM |
| **Hibernate** | Latest | JPA implementation |
| **MariaDB** | 10.6+ | Database |
| **Keycloak Java Client** | 23.0.3 | Keycloak admin API |
| **JavaMail** | - | Email sending |
| **OkHttp** | 4.12.0 | HTTP client (FastAPI calls) |
| **Gson** | 2.11.0 | JSON parsing |
| **Lombok** | Latest | Boilerplate reduction |
| **MapStruct** | 1.6.3 | DTO mapping |
| **MinIO** | 8.6.0 | Object storage |
| **Apache Tika** | 3.2.3 | File type detection |

## 📱 Yêu cầu

- **Java**: JDK 21
- **Maven**: 3.8+
- **MariaDB**: 10.6+
- **Keycloak**: 23.0.3+ (running)
- **FastAPI**: Running on port 8000
- **MinIO**: Optional (for image storage)
- **SMTP Server**: Gmail/Mailgun/etc

---

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/backend-springboot
```

### 2. Install dependencies

```bash
./mvnw clean install
# Windows: mvnw.cmd clean install
```

### 3. Setup MariaDB and Keycloak

```bash
cd smart-trash-can-system/docker
# Docker
docker-compose run -d 
```

**Cấu hình Keycloak:**

1. **Login admin console**: http://localhost:8080
   - Username: `admin`
   - Password: `admin`

2. **Create Realm**: `smart-bin-realm`
   - Click "Create realm" button
   - Name: `smart-bin-realm`
   - Enabled: ON
   - Click "Create"

3. **Create Client**: `smart-bin-client`
   - Client type: OpenID Connect
   - Client ID: `smart-bin-client`
   - Name: `Smart Bin Client`
   - Always display in UI: OFF
   - Client authentication: ON
   - Authorization: OFF
   - Authentication flow:
     - Standard flow: ON
     - Direct access grants: ON (for password grant)
     - Implicit flow: OFF
     - Service accounts roles: ON
   - Valid redirect URIs:
     - `http://localhost:3000/*`
     - `http://localhost:8888/*`
   - Web origins: `*` (development only)
   - Click "Save"

4. **Copy Client Secret**
   - Go to "Credentials" tab
   - Copy "Client secret"
   - Paste vào `application.properties`: `keycloak.client-secret=xxx`

5. **Configure Token Settings** (Optional)
   - Realm Settings → Tokens tab
   - Access Token Lifespan: 5 minutes (300s)
   - Refresh Token Max Reuse: 0
   - SSO Session Idle: 30 minutes
   - SSO Session Max: 10 hours

6. **Email Settings** (for verification)
   - Realm Settings → Email tab
   - From: `noreply@smartbin.com`
   - From display name: `Smart Bin System`
   - Host: `smtp.gmail.com`
   - Port: `587`
   - Enable StartTLS: ON
   - Enable Authentication: ON
   - Username: your Gmail
   - Password: Gmail App Password

---

## 📂 Cấu trúc code

```
src/main/java/com/example/smart_bin_server/
│
├── config/                         # Configuration classes
│   ├── Constants.java             # App constants
│   ├── KeycloakConfig.java        # Keycloak admin client
│   └── SecurityConfig.java        # Spring Security & OAuth2
│
├── controller/                     # REST Controllers
│   ├── AuthController.java        # /api/v1/auth/*
│   ├── DeviceController.java      # /api/v1/devices (CRUD)
│   ├── DeviceDataController.java  # /api/v1/devices/{id}/data
│   ├── NotificationController.java # /api/v1/notifications
│   └── ClassificationController.java # /api/v1/classify-image
│
├── service/                        # Business Logic
│   ├── UserService.java           # User CRUD, email verification
│   ├── KeycloakService.java       # Keycloak admin operations
│   ├── DeviceService.java         # Device CRUD, status check
│   ├── DeviceDataService.java     # Device data management
│   ├── NotificationService.java   # Notification management
│   ├── EmailService.java          # Email sending
│   └── ClassificationService.java # Call FastAPI
│
├── repository/                     # JPA Repositories
│   ├── UserRepository.java
│   ├── DeviceRepository.java
│   ├── DeviceDataRepository.java
│   └── NotificationRepository.java
│
├── model/                          # Entity Models
│   ├── User.java                  # @Entity
│   ├── Device.java                # @Entity
│   ├── DeviceData.java            # @Entity
│   └── Notification.java          # @Entity
│
├── dto/                            # Data Transfer Objects
│   ├── RegisterRequest.java       # record
│   ├── LoginRequest.java          # record
│   ├── AuthResponse.java
│   ├── UserDto.java
│   ├── DeviceDto.java
│   ├── CreateDeviceRequest.java
│   ├── UpdateDeviceRequest.java
│   ├── SendDataRequest.java
│   ├── SendDataResponse.java
│   ├── ClassificationResponse.java
│   └── ...
│
├── mapper/                         # Entity ↔ DTO Mappers
│   ├── DeviceMapper.java
│   └── DeviceDataMapper.java
│
└── SmartBinServerApplication.java  # Main class
```

---

## 📡 API Endpoints

### Tóm tắt

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/v1/auth/register` | POST | ❌ | Đăng ký user |
| `/api/v1/auth/login` | POST | ❌ | Đăng nhập |
| `/api/v1/auth/refresh` | POST | ❌ | Refresh token |
| `/api/v1/auth/logout` | POST | ✅ | Đăng xuất |
| `/api/v1/auth/change-password` | POST | ✅ | Đổi mật khẩu |
| `/api/v1/auth/verify-email` | GET | ❌ | Xác thực email |
| `/api/v1/auth/resend-verification` | POST | ❌ | Gửi lại email |
| `/api/v1/auth/me` | GET | ✅ | Lấy user info |
| `/api/v1/devices` | GET | ✅ | Lấy danh sách devices |
| `/api/v1/devices` | POST | ✅ | Tạo device mới |
| `/api/v1/devices/{id}` | GET | ✅ | Lấy device theo ID |
| `/api/v1/devices/{id}` | PUT | ✅ | Cập nhật device |
| `/api/v1/devices/{id}` | DELETE | ✅ | Xóa device |
| `/api/v1/devices/{id}/data` | POST | ❌ | ESP32 gửi data |
| `/api/v1/devices/{id}/data` | GET | ✅ | Lấy device data |
| `/api/v1/notifications` | GET | ✅ | Lấy notifications |
| `/api/v1/classify-image` | POST | ❌ | Classify ảnh |

### Test API

Sau khi chạy server, test các endpoint:

**Register:**
```bash
curl -X POST http://localhost:8888/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123","firstName":"Test","lastName":"User"}'
```

**Login:**
```bash
curl -X POST http://localhost:8888/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"pass123"}'
```

**Get Devices (with token):**
```bash
curl http://localhost:8888/api/v1/devices \
  -H "Authorization: Bearer {your_access_token}"
```

📝 **Chi tiết API**: Xem Swagger UI tại http://localhost:8888/swagger-ui.html hoặc test bằng Postman collection

---

## 🗄️ Database Schema

### User Table

```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,        -- Keycloak user ID
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiry BIGINT,
    created_at BIGINT,
    updated_at BIGINT
);
```

### Device Table

```sql
CREATE TABLE devices (
    id VARCHAR(255) PRIMARY KEY,        -- MAC address (AA_BB_CC_DD_EE_FF)
    name VARCHAR(255),
    status VARCHAR(50),                 -- ONLINE, OFFLINE
    user_id VARCHAR(255),               -- Foreign key to users
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
```

### Device Data Table

```sql
CREATE TABLE device_data (
    device_id VARCHAR(255) PRIMARY KEY,  -- Foreign key to devices
    recycled_waste_count INT DEFAULT 0,
    non_recycled_waste_count INT DEFAULT 0,
    compostable_waste_count INT DEFAULT 0,
    fill_level INT DEFAULT 0,           -- 0-100%
    is_full BOOLEAN DEFAULT FALSE,
    timestamp BIGINT,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);
```

### Notification Table

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(255),
    message TEXT,
    type VARCHAR(50),                    -- SUCCESS, ERROR, WARNING, INFO
    timestamp BIGINT,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE
);
```

---

## 🔐 Keycloak Integration

### Vai trò

- **Identity Provider**: Quản lý users, authentication
- **OAuth2 Server**: Cấp access token & refresh token
- **JWT Issuer**: Spring Boot verify JWT từ Keycloak

### Các service chính

**KeycloakService.java** - Quản lý users:
- `createUser()` - Đăng ký user mới (disabled)
- `enableUser()` - Enable sau khi verify email
- `login()` - Lấy access token
- `refreshAccessToken()` - Refresh token
- `logout()` - Revoke token
- `updatePassword()` - Đổi mật khẩu

**SecurityConfig.java** - Spring Security:
- Public endpoints: `/api/v1/auth/**`, `/api/v1/classify-image/**`
- Protected endpoints: `/api/v1/devices/**` (cần JWT)
- OAuth2 Resource Server với JWT validation
- CORS configuration
```

---

## � MinIO Storage

### Giới thiệu

MinIO là object storage service tương tự AWS S3, dùng để lưu trữ ảnh từ ESP32-CAM và web/mobile upload.

### Cấu hình MinIO

**application.properties:**
```properties
# MinIO Configuration
minio.url=http://localhost:9000
minio.access-key=minioadmin
minio.secret-key=minioadmin
minio.bucket-name=smart-bin
minio.image-folder=images/
```

**Docker Compose:**
```yaml
minio:
  image: minio/minio:latest
  container_name: smart-bin-minio
  ports:
    - "9000:9000"      # API port
    - "9001:9001"      # Console UI
  environment:
    MINIO_ROOT_USER: minioadmin
    MINIO_ROOT_PASSWORD: minioadmin
  command: server /data --console-address ":9001"
  volumes:
    - ./minio_data:/data
```

### MinIO Console

Truy cập: http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin`

**Tạo Bucket:**
1. Login MinIO Console
2. Buckets → Create Bucket
3. Bucket Name: `smart-bin`
4. Versioning: OFF
5. Object Locking: OFF
6. Quota: None
7. Retention: None
8. Access Policy: Custom (hoặc Public for testing)

### Tính năng MinIO

- Upload ảnh từ ESP32/Web/Mobile
- Tạo presigned URLs (temporary access)
- Tự động tạo bucket nếu chưa tồn tại
- Quản lý ảnh theo deviceId
- S3-compatible (dễ migrate lên AWS S3)

---

## �📧 Email Service

### EmailService.java

Gửi email verification và welcome email.

```java
public void sendVerificationEmail(String toEmail, String firstName, String token) {
    String verifyLink = verificationUrl + "?token=" + token;
    // Build HTML email với link verify
    mailSender.send(message);
}

public void sendWelcomeEmail(String toEmail, String firstName) {
    // Send welcome email sau khi verify
}
```

### Gmail App Password

Nếu dùng Gmail, cần tạo App Password:

1. Google Account → Security
2. 2-Step Verification → ON
3. App passwords → Generate
4. Copy password vào `application.properties`

---

## ⏰ Scheduled Tasks

### Device Status Monitor

**Chức năng:** Tự động kiểm tra device status mỗi 10 giây

**Logic:**
- Nếu device không gửi data trong 60 giây → Set status = `OFFLINE`
- Tạo notification cảnh báo `Device disconnected`

**Config trong DeviceService.java:**
```java
@Scheduled(fixedRate = 10000) // 10 seconds
public void checkDevicesStatus() { ... }
```

---

## 🚀 Chạy ứng dụng

### Development Mode

```bash
./mvnw spring-boot:run

# With profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
---

## 🐛 Troubleshooting

### 1. Không kết nối MariaDB

```bash
# Test connection
mysql -h localhost -u root -p smart_bin_db

# Check port
netstat -an | grep 3306
```

### 2. Keycloak 401 Unauthorized

- Kiểm tra `issuer-uri` đúng realm
- Kiểm tra `jwk-set-uri` accessible
- Kiểm tra token không expired
- Clear browser cache

### 3. Email không gửi được

- Kiểm tra Gmail App Password
- Enable "Less secure app access" (nếu cần)
- Check firewall port 587

### 4. FastAPI unreachable

```bash
# Test FastAPI
curl http://localhost:8000/docs

# Check trong Docker network
docker network inspect smart-bin-network
```

---

## 📚 Tài liệu tham khảo

- [Spring Boot Docs](https://spring.io/projects/spring-boot)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)

---

<div align="center">
  <p>Made with ❤️ for Smart Bin System</p>
</div>