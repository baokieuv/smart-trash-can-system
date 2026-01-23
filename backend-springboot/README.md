# 🌐 Smart Bin Backend (Spring Boot)

API server chính của hệ thống Smart Bin, xử lý authentication, device management, AI classification, và real-time device data với bảo mật cao.

---

## 📋 Mục lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Công nghệ](#-công-nghệ)
- [Yêu cầu](#-yêu-cầu)
- [Cài đặt](#-cài-đặt)
- [Cấu hình](#-cấu-hình)
- [Cấu trúc code](#-cấu-trúc-code)
- [API Endpoints](#-api-endpoints)
- [Database Schema](#-database-schema)
- [Security](#-security)
- [Keycloak Integration](#-keycloak-integration)
- [MinIO Storage](#-minio-storage)
- [Redis Cache](#-redis-cache)
- [Email Service](#-email-service)
- [Scheduled Tasks](#-scheduled-tasks)
- [Chạy ứng dụng](#-chạy-ứng-dụng)
- [Troubleshooting](#-troubleshooting)

---

## 🎯 Giới thiệu

Spring Boot backend là core API server của Smart Bin System, cung cấp:

- **RESTful APIs** cho Web & Mobile clients
- **Authentication/Authorization** với Keycloak OAuth2 + JWT
- **Device Management** - CRUD operations và real-time status monitoring
- **Secure Device Data** - HMAC-SHA256 signature verification cho ESP32
- **AI Classification** - Tích hợp FastAPI để phân loại rác thải
- **Image Storage** - MinIO S3-compatible object storage
- **Email Service** - Verification và notification emails
- **Notifications** - Real-time event tracking system
- **Scheduled Tasks** - Auto device health monitoring

---

## ✨ Tính năng

### 🔐 Authentication & Authorization
- ✅ **User Registration** - Tạo account với Keycloak (disabled ban đầu)
- ✅ **Email Verification** - Token-based với expiry time (10 phút)
- ✅ **Login** - OAuth2 Resource Owner Password Grant
- ✅ **Refresh Token** - Gia hạn session không cần login lại
- ✅ **Logout** - Revoke refresh token khỏi Keycloak
- ✅ **Change Password** - Yêu cầu current password để đổi
- ✅ **Forgot Password** - Reset password qua email
- ✅ **JWT Validation** - Spring Security OAuth2 Resource Server
- ✅ **Resend Verification** - Gửi lại email verification
- ✅ **Get Current User** - Lấy thông tin user từ JWT

### 🎛️ Device Management
- ✅ **CRUD Operations** - Create, Read, Update, Delete devices
- ✅ **Ownership Validation** - Chỉ owner mới thao tác được device
- ✅ **Status Tracking** - Real-time ONLINE/OFFLINE status
- ✅ **MAC Address ID** - Format: `AA_BB_CC_DD_EE_FF`
- ✅ **Auto Status Check** - Scheduled task chạy mỗi 2 phút
- ✅ **Auto Offline** - Device offline nếu không gửi data trong 60 giây
- ✅ **Device List** - Filter theo userId
- ✅ **Device Details** - Get thông tin chi tiết device

### 📊 Secure Device Data
- ✅ **HMAC-SHA256 Signature** - Verify request từ ESP32
- ✅ **Nonce-based Security** - Redis TTL 5 phút, prevent replay attacks
- ✅ **Waste Counting** - Recyclable, Non-recyclable, Compostable
- ✅ **Fill Level** - Tracking 0-100%
- ✅ **Battery Level** - Optional field
- ✅ **Full Bin Detection** - Auto set `is_full` flag
- ✅ **Timestamp Tracking** - Mỗi data point có timestamp
- ✅ **Data History** - Get latest device data

### 🔔 Notifications
- ✅ **User Notifications** - Filter theo userId
- ✅ **Device Events** - Connect/disconnect, full bin, errors
- ✅ **Notification Types** - SUCCESS, ERROR, WARNING, INFO
- ✅ **Read/Unread Status** - Track notification status
- ✅ **Pagination** - Efficient data loading
- ✅ **Update Status** - Mark as read/unread
- ✅ **Device Name** - Include trong notification

### 🤖 AI Classification
- ✅ **Image Classification** - Call FastAPI endpoint
- ✅ **Multipart Upload** - Support nhiều image formats
- ✅ **Classification Result** - Label, Confidence, Category
- ✅ **Classification Logs** - Lưu history với imageUrl
- ✅ **Device Linking** - Link classification với device và user
- ✅ **MinIO Integration** - Auto upload image to storage

### 📦 MinIO Storage
- ✅ **S3-Compatible** - Dễ migrate lên AWS S3
- ✅ **Auto Bucket Creation** - Tự tạo bucket nếu chưa có
- ✅ **File Validation** - MIME type và extension check
- ✅ **Multiple Formats** - JPEG, PNG, GIF, WebP, HEIC, HEIF
- ✅ **Structured Path** - `waste/image_{timestamp}_{random}.ext`
- ✅ **Presigned URLs** - Temporary access (optional)
- ✅ **File Metadata** - Content-Type detection với Apache Tika

---

## 🚀 Công nghệ

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 21 | Programming language |
| **Spring Boot** | 4.0.0 | Application framework |
| **Spring Security** | 6.x | OAuth2 Resource Server |
| **Spring Data JPA** | Latest | Database ORM |
| **Spring Data Redis** | Latest | Redis integration |
| **Hibernate** | Latest | JPA implementation |
| **MariaDB** | 10.6+ | Relational Database |
| **Redis** | Latest | Cache & Session Store |
| **Keycloak** | 23.0.3 | Identity & Access Management |
| **Spring Mail** | Latest | Email sending (SMTP) |
| **OkHttp** | 4.12.0 | HTTP client (FastAPI calls) |
| **Gson** | 2.11.0 | JSON serialization |
| **Lombok** | 1.18.42 | Boilerplate reduction |
| **MapStruct** | 1.6.3 | Entity ↔ DTO mapping |
| **MinIO** | 8.6.0 | S3-compatible object storage |
| **Apache Tika** | 3.2.3 | File type detection |
| **Auth0 JWT** | 4.4.0 | JWT utilities |
| **HikariCP** | Latest | Connection pooling |

---

## 📱 Yêu cầu

### Phần mềm cần thiết:
- **Java**: JDK 21 hoặc mới hơn
- **Maven**: 3.8+ hoặc dùng Maven Wrapper (mvnw)
- **Docker & Docker Compose**: Latest version

### Services (chạy qua Docker):
- **MariaDB**: 10.6+ → Port 3333
- **Redis**: Latest → Port 6379
- **Keycloak**: 23.0.3+ → Port 8080
- **MinIO**: Latest → Port 9000 (API), 9001 (Console)
- **PostgreSQL**: 16+ → Port 5432 (cho Keycloak)

### External Services:
- **FastAPI Server**: Running on port 8000
- **Gmail SMTP**: Với App Password (cho email service)

---

## 🛠️ Cài đặt

### 1. Clone Repository

```bash
git clone https://github.com/baokieuv/smart-trash-can-system.git
cd smart-trash-can-system/backend-springboot
```

### 2. Setup Docker Services

Chạy tất cả services bằng Docker Compose:

```bash
cd ../docker
docker-compose up -d
```

**Verify services:**

```bash
docker ps
```

Kiểm tra các containers đang chạy:
- `smart-bin-mariadb`
- `smart-bin-redis`
- `smart-bin-keycloak`
- `smart-bin-minio`
- `smart-bin-postgres`

**Test connections:**

```bash
# MariaDB
mysql -h localhost -P 3333 -u admin -padmin smart-bin

# Redis
redis-cli -p 6379 ping
# Should return: PONG

# Keycloak
curl http://localhost:8080/health/ready

# MinIO
curl http://localhost:9000/minio/health/live
```

### 3. Cấu hình Keycloak

#### Login Admin Console

Truy cập: http://localhost:8080
- Username: `admin`
- Password: `admin`

#### 1) Create Realm

1. Click dropdown "**master**" góc trái trên
2. Click "**Create realm**"
3. Realm name: `smart-bin`
4. Enabled: ✅ ON
5. Click "**Create**"

#### 2) Create Client

1. Clients → "**Create client**"
2. **General Settings:**
   - Client type: `OpenID Connect`
   - Client ID: `smart-bin-client`
   - Name: `Smart Bin Client`
   - Description: `OAuth2 client for Smart Bin System`
   - Always display in UI: ❌ OFF
   - Click "**Next**"

3. **Capability config:**
   - Client authentication: ✅ ON (confidential client)
   - Authorization: ❌ OFF
   - Authentication flow:
     - ✅ Standard flow
     - ✅ Direct access grants (for password grant)
     - ✅ Service accounts roles
     - ❌ Implicit flow
     - ❌ OAuth 2.0 Device Authorization Grant
   - Click "**Next**"

4. **Login settings:**
   - Root URL: `http://localhost:3000`
   - Home URL: `http://localhost:3000`
   - Valid redirect URIs:
     - `http://localhost:3000/*`
     - `http://localhost:8888/*`
   - Valid post logout redirect URIs: `+`
   - Web origins: `*` (dev only, production nên specific)
   - Click "**Save**"

#### 3) Copy Client Secret

1. Go to "**Credentials**" tab
2. Copy "**Client secret**" value
3. Update `application.yaml`:

```yaml
keycloak:
  client-secret: YOUR_CLIENT_SECRET_HERE
```

#### 4) Token Configuration (Optional)

Realm Settings → Tokens tab:

```
Access Token Lifespan: 5 minutes
Refresh Token Max Reuse: 0
SSO Session Idle: 30 minutes
SSO Session Max: 10 hours
```

### 4. Cấu hình MinIO

#### Login MinIO Console

Truy cập: http://localhost:9001
- Username: `minioadmin`
- Password: `minioadmin`

#### Create Bucket

1. **Buckets** → "**Create Bucket**"
2. Bucket Name: `smart-bin`
3. Versioning: ❌ OFF
4. Object Locking: ❌ OFF
5. Quota: None
6. Retention: None
7. Click "**Create Bucket**"

---

### 5. Cấu hình Gmail SMTP

#### Tạo Gmail App Password

1. Vào [Google Account Security](https://myaccount.google.com/security)
2. Enable "**2-Step Verification**" (bắt buộc)
3. Search "**App passwords**" hoặc vào https://myaccount.google.com/apppasswords
4. Select app: "**Mail**"
5. Select device: "**Other (Custom name)**"
6. Name: `Smart Bin Server`
7. Click "**Generate**"
8. Copy password (16 ký tự, không có spaces)

#### Update Configuration

Edit `application.yaml`:

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 465
    username: your-email@gmail.com
    password: xxxx xxxx xxxx xxxx  # App password

app:
  email:
    from: your-email@gmail.com
```

### 6. Build Project

```bash
cd ../backend-springboot

# Maven Wrapper (recommended)
./mvnw clean install

# Windows
mvnw.cmd clean install

# Hoặc dùng Maven global
mvn clean install
```

---

## ⚙️ Cấu hình

### application.yaml

File cấu hình chính: `src/main/resources/application.yaml`

```yaml
spring:
  application:
    name: smart-bin-server

  # Database Configuration
  datasource:
    url: jdbc:mariadb://localhost:3333/smart-bin
    username: admin
    password: admin
    driver-class-name: org.mariadb.jdbc.Driver
    
  jpa:
    hibernate:
      ddl-auto: update  # Tự động tạo/update tables
    show-sql: true      # Log SQL queries
    
  # OAuth2 Resource Server
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8080/realms/smart-bin
          jwk-set-uri: http://localhost:8080/realms/smart-bin/protocol/openid-connect/certs
          
  # Email Configuration
  mail:
    host: smtp.gmail.com
    port: 465
    username: your-email@gmail.com
    password: your-app-password
    
  # Redis Configuration
  data:
    redis:
      host: localhost
      port: 6379

server:
  port: 8888

# Application Settings
app:
  secret-key: MY_SUPER_SECRET_KEY_1234  # Cho HMAC signature
  ai-server:
    url: http://localhost:8000
  email:
    from: your-email@gmail.com
    verification:
      url: http://localhost:8888/api/v1/auth/verify-email

# Keycloak Admin Settings
keycloak:
  server-url: http://localhost:8080
  realm: smart-bin
  client-id: smart-bin-client
  client-secret: YOUR_CLIENT_SECRET
  admin-username: admin
  admin-password: admin

# MinIO Settings
minio:
  url: http://localhost:9000
  bucket: smart-bin
  access_key: minioadmin
  secret_key: minioadmin
```

### Environment Variables (Production)

Để override config cho production, dùng environment variables:

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:mariadb://prod-db:3306/smart-bin
SPRING_DATASOURCE_USERNAME=prod_user
SPRING_DATASOURCE_PASSWORD=secure_password

# Keycloak
KEYCLOAK_SERVER_URL=https://keycloak.production.com
KEYCLOAK_CLIENT_SECRET=production_secret

# MinIO
MINIO_SERVER_URL=https://minio.production.com
MINIO_ACCESS_KEY=prod_access
MINIO_SECRET_KEY=prod_secret

# Email
SPRING_MAIL_USERNAME=noreply@smartbin.com
SPRING_MAIL_PASSWORD=prod_app_password

# Redis
REDIS_HOST=prod-redis
REDIS_PORT=6379

# Server
SERVER_PORT=8888
```

---

## 📂 Cấu trúc Code

```
src/main/java/com/example/smart_bin_server/
│
├── config/                              # Configuration Classes
│   ├── Constants.java                   # App constants (DeviceStatus, LogType, etc.)
│   ├── KeycloakConfig.java              # Keycloak admin client bean
│   ├── MinioConfig.java                 # MinIO client bean
│   ├── RedisConfig.java                 # Redis configuration (nếu có)
│   └── SecurityConfig.java              # Spring Security + OAuth2
│
├── controller/                          # REST API Controllers
│   ├── AuthController.java              # /api/v1/auth/*
│   ├── DeviceController.java            # /api/v1/devices (CRUD)
│   ├── DeviceDataController.java        # /api/v1/devices/{id}/data
│   ├── NotificationController.java      # /api/v1/notifications
│   └── ClassificationController.java    # /api/v1/classify-image
│
├── service/                             # Business Logic Layer
│   ├── UserService.java                 # User CRUD, verification, auth
│   ├── KeycloakService.java             # Keycloak admin operations
│   ├── DeviceService.java               # Device CRUD, status check
│   ├── DeviceDataService.java           # Secure data handling, HMAC
│   ├── NotificationService.java         # Notification management
│   ├── EmailService.java                # Email sending
│   ├── ClassificationService.java       # FastAPI integration
│   └── MinioService.java                # File upload, storage
│
├── repository/                          # Data Access Layer (JPA)
│   ├── UserRepository.java              # User CRUD
│   ├── DeviceRepository.java            # Device CRUD
│   ├── DeviceDataRepository.java        # Device data CRUD
│   ├── NotificationRepository.java      # Notification CRUD
│   └── ClassificationLogsRepository.java # Classification history
│
├── model/                               # Entity Models (JPA)
│   ├── User.java                        # @Entity - users table
│   ├── Device.java                      # @Entity - devices table
│   ├── DeviceData.java                  # @Entity - device_data table
│   ├── Notification.java                # @Entity - notifications table
│   └── ClassificationLogs.java          # @Entity - classification_logs table
│
├── dto/                                 # Data Transfer Objects
│   ├── RegisterRequest.java             # record - registration payload
│   ├── LoginRequest.java                # record - login payload
│   ├── ChangePasswordRequest.java       # record - change password
│   ├── ForgotPasswordRequest.java       # record - forgot password
│   ├── VerifyEmailRequest.java          # record - email verification
│   ├── AuthResponse.java                # Authentication response
│   ├── TokenResponse.java               # Token response
│   ├── UserDto.java                     # User DTO
│   ├── DeviceDto.java                   # Device DTO
│   ├── CreateDeviceRequest.java         # record - create device
│   ├── UpdateDeviceRequest.java         # record - update device
│   ├── SendDataRequest.java             # record - ESP32 data payload
│   ├── SendDataResponse.java            # Send data response
│   ├── DeviceDataDto.java               # Device data DTO
│   ├── NotificationDto.java             # Notification DTO
│   ├── UpdateNotiStatus.java            # Update notification status
│   └── ClassificationResponse.java      # record - AI classification result
│
├── mapper/                              # Entity ↔ DTO Mappers (MapStruct)
│   ├── DeviceMapper.java                # Device mapping
│   └── DeviceDataMapper.java            # Device data mapping
│
├── exception/                           # Custom Exceptions (nếu có)
│   └── ...
│
└── SmartBinServerApplication.java       # Main Application Class
```

---

## 📡 API Endpoints

### Base URL
```
http://localhost:8888/api/v1
```

### Authentication Endpoints

#### 1. Register User

```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "firstName": "John",
  "lastName": "Doe"
}
```

**Response:**
```json
{
  "message": "Registration successful. Please check your email to verify your account.",
  "user": {
    "id": "uuid",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "emailVerified": false
  }
}
```

#### 2. Login

```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGci...",
  "refreshToken": "eyJhbGci...",
  "expiresIn": 300,
  "tokenType": "Bearer"
}
```

#### 3. Refresh Token

```http
POST /auth/refresh
Content-Type: application/json

{
  "refreshToken": "eyJhbGci..."
}
```

#### 4. Logout

```http
POST /auth/logout
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "refreshToken": "eyJhbGci..."
}
```

#### 5. Change Password

```http
POST /auth/change-password
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "currentPassword": "oldpass123",
  "newPassword": "newpass123",
  "confirmPassword": "newpass123"
}
```

#### 6. Verify Email

```http
GET /auth/verify-email?token={verification_token}
```

#### 7. Resend Verification Email

```http
POST /auth/resend-verification
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 8. Forgot Password

```http
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}
```

#### 9. Get Current User

```http
GET /auth/me
Authorization: Bearer {access_token}
```

### Device Endpoints

#### 1. Create Device

```http
POST /devices
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "macAddress": "AA:BB:CC:DD:EE:FF",
  "name": "Kitchen Bin"
}
```

**Response:**
```json
{
  "id": "AA_BB_CC_DD_EE_FF",
  "name": "Kitchen Bin",
  "status": "OFFLINE",
  "userId": "user-uuid",
  "createdAt": 1234567890000,
  "updatedAt": 1234567890000
}
```

#### 2. Get All Devices

```http
GET /devices
Authorization: Bearer {access_token}
```

#### 3. Get Device by ID

```http
GET /devices/{deviceId}
Authorization: Bearer {access_token}
```

#### 4. Update Device

```http
PUT /devices/{deviceId}
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "name": "New Device Name",
  "status": "ONLINE"
}
```

#### 5. Delete Device

```http
DELETE /devices/{deviceId}
Authorization: Bearer {access_token}
```

### Device Data Endpoints (ESP32)

#### 1. Get Nonce

```http
GET /devices/{deviceId}/nonce
```

**Response:**
```json
{
  "nonce": "random-uuid",
  "expiresIn": 300
}
```

#### 2. Send Device Data

```http
POST /devices/{deviceId}/data
Content-Type: application/json
X-Signature: {hmac_sha256_signature}

{
  "recycledWasteCount": 10,
  "nonRecycledWasteCount": 5,
  "compostableWasteCount": 3,
  "fillLevel": 45,
  "batteryLevel": 85,
  "nonce": "nonce-from-step-1"
}
```

**Signature calculation (ESP32):**
```c
String payload = "{json_body}";
String signature = HMAC_SHA256(payload, SECRET_KEY);
```

**Response:**
```json
{
  "message": "Data received successfully",
  "timestamp": 1234567890000
}
```

#### 3. Get Device Data

```http
GET /devices/{deviceId}/data
Authorization: Bearer {access_token}
```

### Notification Endpoints

#### 1. Get Notifications

```http
GET /notifications?page=0&size=20
Authorization: Bearer {access_token}
```

**Response:**
```json
{
  "content": [
    {
      "id": 1,
      "userId": "user-uuid",
      "deviceId": "device-id",
      "deviceName": "Kitchen Bin",
      "message": "Device connected",
      "type": "SUCCESS",
      "status": "UNREAD",
      "timestamp": 1234567890000
    }
  ],
  "totalPages": 5,
  "totalElements": 100
}
```

#### 2. Update Notification Status

```http
PUT /notifications/{notificationId}
Authorization: Bearer {access_token}
Content-Type: application/json

{
  "status": "READ"
}
```

### Classification Endpoint

#### Classify Image

```http
POST /classify-image
Content-Type: multipart/form-data

deviceId: AA_BB_CC_DD_EE_FF
image: [binary file]
```

**Response:**
```json
{
  "Label": "plastic bottle",
  "Confident": 0.95,
  "Category": "recyclable"
}
```

---

## 🗄️ Database Schema

### 1. Users Table

```sql
CREATE TABLE users (
    id VARCHAR(255) PRIMARY KEY,              -- Keycloak user ID
    email VARCHAR(255) UNIQUE NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email_verified BOOLEAN DEFAULT FALSE,
    verification_token VARCHAR(255),
    verification_token_expiry BIGINT,         -- Unix timestamp (ms)
    created_at BIGINT,
    updated_at BIGINT,
    INDEX idx_email (email),
    INDEX idx_verification_token (verification_token)
);
```

### 2. Devices Table

```sql
CREATE TABLE devices (
    id VARCHAR(255) PRIMARY KEY,              -- MAC address (AA_BB_CC_DD_EE_FF)
    name VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,              -- ONLINE, OFFLINE
    user_id VARCHAR(255),
    created_at BIGINT,
    updated_at BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
);
```

### 3. Device Data Table

```sql
CREATE TABLE device_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(255) NOT NULL,
    recycled_waste_count INT DEFAULT 0,
    non_recycled_waste_count INT DEFAULT 0,
    compostable_waste_count INT DEFAULT 0,
    fill_level INT DEFAULT 0,                 -- 0-100%
    battery_level INT DEFAULT 100,            -- 0-100%
    is_full BOOLEAN DEFAULT FALSE,
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    INDEX idx_device_timestamp (device_id, timestamp DESC)
);
```

### 4. Notifications Table

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    device_id VARCHAR(255),
    device_name VARCHAR(255),
    message TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,                -- SUCCESS, ERROR, WARNING, INFO
    status VARCHAR(50) DEFAULT 'UNREAD',      -- READ, UNREAD
    timestamp BIGINT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL,
    INDEX idx_user_timestamp (user_id, timestamp DESC),
    INDEX idx_status (status)
);
```

### 5. Classification Logs Table

```sql
CREATE TABLE classification_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    device_id VARCHAR(255),
    user_id VARCHAR(255),
    image_url VARCHAR(500),
    label VARCHAR(100),                       -- Object detected (e.g., "plastic bottle")
    category VARCHAR(50),                     -- recyclable, non-recyclable, compostable
    confidence DOUBLE,                        -- 0.0 to 1.0
    timestamp BIGINT,
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE SET NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_device_timestamp (device_id, timestamp DESC),
    INDEX idx_user_timestamp (user_id, timestamp DESC)
);
```

---

## 🔒 Security

### 1. OAuth2 + JWT Authentication

- **Keycloak** làm Authorization Server
- **Spring Security** làm Resource Server
- **JWT** để authentication (stateless)
- Access token có thời hạn ngắn (5 phút)
- Refresh token để gia hạn (10 giờ)

### 2. HMAC Signature Verification (ESP32)

**Flow:**

1. ESP32 gọi `GET /devices/{id}/nonce` → Nhận nonce
2. ESP32 tạo payload JSON
3. ESP32 tính HMAC-SHA256: `signature = HMAC(payload, SECRET_KEY)`
4. ESP32 gửi `POST /devices/{id}/data` với header `X-Signature: {signature}`
5. Server verify signature:
   - Tính lại HMAC từ payload
   - So sánh với signature trong header
   - Kiểm tra nonce trong Redis (chưa dùng + TTL còn hạn)
   - Mark nonce đã dùng → Không reuse được

**Code example (ESP32):**

```cpp
#include <mbedtls/md.h>

String calculateHMAC(String payload, String key) {
  byte hmac[32];
  mbedtls_md_context_t ctx;
  mbedtls_md_type_t md_type = MBEDTLS_MD_SHA256;
  
  mbedtls_md_init(&ctx);
  mbedtls_md_setup(&ctx, mbedtls_md_info_from_type(md_type), 1);
  mbedtls_md_hmac_starts(&ctx, (byte*)key.c_str(), key.length());
  mbedtls_md_hmac_update(&ctx, (byte*)payload.c_str(), payload.length());
  mbedtls_md_hmac_finish(&ctx, hmac);
  mbedtls_md_free(&ctx);
  
  String signature = "";
  for (int i = 0; i < 32; i++) {
    signature += String(hmac[i], HEX);
  }
  return signature;
}
```

### 3. Password Security

- Keycloak hash passwords với bcrypt
- Password policy configurable (min length, complexity)
- Change password yêu cầu current password
- Forgot password qua email token

### 4. CORS Configuration

```java
// Development
allowedOrigins: "*"

// Production (recommended)
allowedOrigins: [
  "https://yourdomain.com",
  "https://app.yourdomain.com"
]
```

### 5. Rate Limiting (TODO)

Implement rate limiting với Redis:
- Login attempts: 5/minute
- API calls: 100/minute per user
- Email sends: 3/hour per email

---

## 🔐 Keycloak Integration

### Vai trò

- **Identity Provider** - Quản lý users và credentials
- **OAuth2 Authorization Server** - Cấp access/refresh tokens
- **JWT Issuer** - Generate và sign JWTs
- **Admin API** - CRUD users, reset passwords

### KeycloakService Methods

```java
// User Management
public String createUser(RegisterRequest request)
public void enableUser(String userId)
public void deleteUser(String userId)

// Authentication
public TokenResponse login(String username, String password)
public TokenResponse refreshAccessToken(String refreshToken)
public void logout(String refreshToken)

// Password Management
public void updatePassword(String userId, String newPassword)
public void resetPasswordViaEmail(String userId)
```

### OAuth2 Flow (Password Grant)

```
┌─────────┐                ┌──────────┐              ┌──────────┐
│ Client  │                │  Spring  │              │ Keycloak │
│(Web/App)│                │   Boot   │              │  Server  │
└────┬────┘                └─────┬────┘              └─────┬────┘
     │                           │                         │
     │  POST /auth/login         │                         │
     │  {email, password}        │                         │
     ├──────────────────────────>│                         │
     │                           │                         │
     │                           │  Token Request          │
     │                           │  (password grant)       │
     │                           ├────────────────────────>│
     │                           │                         │
     │                           │  JWT tokens             │
     │                           │<────────────────────────┤
     │                           │                         │
     │  {accessToken, refresh..} │                         │
     │<──────────────────────────┤                         │
     │                           │                         │
     │  GET /devices             │                         │
     │  Authorization: Bearer..  │                         │
     ├──────────────────────────>│                         │
     │                           │                         │
     │                           │  Verify JWT             │
     │                           ├────────────────────────>│
     │                           │  (JWK Set)              │
     │                           │<────────────────────────┤
     │                           │                         │
     │  Device list              │                         │
     │<──────────────────────────┤                         │
     │                           │                         │
```

### JWT Structure

```json
{
  "header": {
    "alg": "RS256",
    "typ": "JWT",
    "kid": "key-id"
  },
  "payload": {
    "sub": "user-uuid",
    "email": "user@example.com",
    "name": "John Doe",
    "iss": "http://localhost:8080/realms/smart-bin",
    "aud": "smart-bin-client",
    "exp": 1234567890,
    "iat": 1234567590
  },
  "signature": "..."
}
```

---

## 📦 MinIO Storage

### Tính năng

- **S3-Compatible API** - Dễ migrate lên AWS S3
- **Local Development** - Không cần AWS credentials
- **Presigned URLs** - Temporary access không cần authentication
- **Bucket Management** - Auto create buckets
- **File Validation** - MIME type check

### MinioService Methods

```java
public String uploadFile(MultipartFile file) throws Exception
public InputStream downloadFile(String objectName) throws Exception
public String getPresignedUrl(String objectName, int expiryMinutes)
public void deleteFile(String objectName)
public List<String> listFiles(String prefix)
```

### Upload Flow

```
┌──────────┐         ┌──────────┐         ┌──────┐
│  Client  │         │  Spring  │         │ MinIO│
│          │         │   Boot   │         │      │
└─────┬────┘         └─────┬────┘         └───┬──┘
      │                    │                  │
      │ POST /classify     │                  │
      │ image: [file]      │                  │
      ├───────────────────>│                  │
      │                    │                  │
      │                    │  Upload image    │
      │                    ├─────────────────>│
      │                    │                  │
      │                    │  imageUrl        │
      │                    │<─────────────────┤
      │                    │                  │
      │                    │  Call FastAPI    │
      │                    │  (classification)│
      │                    │                  │
      │                    │  Save logs       │
      │                    │  (with imageUrl) │
      │                    │                  │
      │  Classification    │                  │
      │  result            │                  │
      │<───────────────────┤                  │
      │                    │                  │
```

### File Naming Convention

```
waste/image_{timestamp}_{random}.{ext}

Example:
waste/image_1640000000000_abc123.jpg
```

---

## 🔴 Redis Cache

### Use Cases

#### 1. Nonce Storage (HMAC Verification)

```java
// Store nonce với TTL 5 phút
String nonce = UUID.randomUUID().toString();
redisTemplate.opsForValue().set(
    "nonce:" + deviceId + ":" + nonce,
    "unused",
    5,
    TimeUnit.MINUTES
);

// Check và mark nonce đã dùng
String key = "nonce:" + deviceId + ":" + nonce;
String value = redisTemplate.opsForValue().get(key);
if (value == null || value.equals("used")) {
    throw new RuntimeException("Invalid or expired nonce");
}
redisTemplate.opsForValue().set(key, "used");
```

#### 2. Session Management (Optional)

```java
// Store refresh token
redisTemplate.opsForValue().set(
    "session:" + userId,
    refreshToken,
    10,
    TimeUnit.HOURS
);
```

#### 3. Rate Limiting (TODO)

```java
// API rate limiting
String key = "rate:" + userId + ":" + endpoint;
Long count = redisTemplate.opsForValue().increment(key);
if (count == 1) {
    redisTemplate.expire(key, 1, TimeUnit.MINUTES);
}
if (count > 100) {
    throw new RateLimitException();
}
```

---

## 📧 Email Service

### EmailService Methods

```java
public void sendVerificationEmail(String email, String firstName, String token)
public void sendWelcomeEmail(String email, String firstName)
public void sendPasswordResetEmail(String email, String resetLink)
public void sendDeviceAlertEmail(String email, String deviceName, String message)
```

### Email Templates

#### Verification Email

```html
<!DOCTYPE html>
<html>
<head>
    <style>
        body { font-family: Arial, sans-serif; }
        .button {
            background-color: #4CAF50;
            color: white;
            padding: 15px 32px;
            text-decoration: none;
            display: inline-block;
        }
    </style>
</head>
<body>
    <h2>Welcome to Smart Bin System!</h2>
    <p>Hi {{firstName}},</p>
    <p>Thank you for registering. Please verify your email:</p>
    <a href="{{verifyLink}}" class="button">Verify Email</a>
    <p>This link will expire in 10 minutes.</p>
</body>
</html>
```

### Gmail Configuration

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 465
    username: your-email@gmail.com
    password: your-app-password
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
```

**Lưu ý:**
- Phải enable 2FA trên Gmail
- Tạo App Password (không dùng password thường)
- Port 465 (SSL) hoặc 587 (TLS)

---

## ⏰ Scheduled Tasks

### Device Status Monitor

**File:** `DeviceService.java`

```java
@Scheduled(fixedRate = 2 * 60000) // Chạy mỗi 2 phút
public void checkDevicesStatus() {
    List<Device> devices = repository.findByStatus("ONLINE");
    long now = System.currentTimeMillis();
    
    for (Device device : devices) {
        DeviceData data = dataRepository
            .findFirstByDeviceIdOrderByTimestampDesc(device.getId())
            .orElse(null);
        
        if (data == null) continue;
        
        // Nếu không gửi data trong 60 giây → Set OFFLINE
        if (now - data.getTimestamp() > 60000) {
            device.setStatus("OFFLINE");
            
            // Tạo notification
            Notification notification = new Notification();
            notification.setUserId(device.getUserId());
            notification.setDeviceId(device.getId());
            notification.setDeviceName(device.getName());
            notification.setType("WARNING");
            notification.setMessage("Device disconnected.");
            notification.setTimestamp(now);
            
            notificationService.addNotification(notification);
        }
    }
    
    repository.saveAll(devices);
}
```

### Cấu hình

```yaml
spring:
  task:
    scheduling:
      pool:
        size: 5
```

---

## 🚀 Chạy Ứng Dụng

### Development Mode

```bash
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run

# With specific profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Build JAR

```bash
./mvnw clean package

# Skip tests
./mvnw clean package -DskipTests
```

### Run JAR

```bash
java -jar target/smart-bin-server-0.0.1-SNAPSHOT.jar

# With profile
java -jar target/smart-bin-server-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker Build

```bash
# Build image
docker build -t smart-bin-backend:latest .

# Run container
docker run -d \
  --name smart-bin-backend \
  --network smart-bin-network \
  -p 8888:8888 \
  -e SPRING_DATASOURCE_URL=jdbc:mariadb://mariadb:3306/smart-bin \
  -e KEYCLOAK_SERVER_URL=http://keycloak:8080 \
  -e MINIO_SERVER_URL=http://minio:9000 \
  -e REDIS_HOST=redis \
  smart-bin-backend:latest
```

### Health Check

```bash
# API health
curl http://localhost:8888/actuator/health

# Test endpoint
curl http://localhost:8888/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"password123"}'
```

---

## 🐛 Troubleshooting

### 1. Không kết nối MariaDB

**Error:** `Could not open JPA EntityManager for transaction`

**Solution:**

```bash
# Test connection
mysql -h localhost -P 3333 -u admin -padmin smart-bin

# Check container
docker ps | grep mariadb

# Check logs
docker logs smart-bin-mariadb

# Restart container
docker restart smart-bin-mariadb
```

### 2. Keycloak 401 Unauthorized

**Error:** `JWT expired` hoặc `Invalid token`

**Reasons:**
- Token đã hết hạn → Dùng refresh token
- Issuer URI sai → Kiểm tra `issuer-uri` trong config
- JWK Set không access được → Kiểm tra network

**Solution:**

```bash
# Test Keycloak
curl http://localhost:8080/realms/smart-bin/.well-known/openid-configuration

# Test JWK Set
curl http://localhost:8080/realms/smart-bin/protocol/openid-connect/certs

# Verify JWT token
# Copy token và paste vào https://jwt.io
```

### 3. Email không gửi được

**Error:** `Authentication failed`

**Solution:**

- ✅ Enable 2FA trên Gmail
- ✅ Tạo App Password mới
- ✅ Dùng đúng port (465 với SSL, 587 với TLS)
- ✅ Check firewall không block port

```yaml
# Try TLS instead of SSL
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

### 4. MinIO Connection Failed

**Error:** `The specified bucket does not exist`

**Solution:**

```bash
# Test MinIO API
curl http://localhost:9000/minio/health/live

# Check bucket
docker exec -it smart-bin-minio mc ls local

# Create bucket manually
docker exec -it smart-bin-minio mc mb local/smart-bin
```

### 5. Redis Connection Error

**Error:** `Unable to connect to Redis`

**Solution:**

```bash
# Test Redis
redis-cli -h localhost -p 6379 ping

# Check container
docker ps | grep redis

# Restart
docker restart smart-bin-redis
```

### 6. FastAPI Unreachable

**Error:** `Connection refused to localhost:8000`

**Solution:**

```bash
# Check FastAPI running
curl http://localhost:8000/docs

# Start FastAPI
cd model-fastapi
python server.py
```

### 7. HMAC Signature Invalid

**Error:** `Invalid signature`

**Reasons:**
- Secret key khác nhau (ESP32 vs Server)
- Payload format sai
- Nonce expired hoặc đã dùng

**Debug:**

```java
// Server side - log để compare
log.info("Payload: {}", payload);
log.info("Expected signature: {}", calculatedSignature);
log.info("Received signature: {}", receivedSignature);
```

```cpp
// ESP32 side
Serial.println("Payload: " + payload);
Serial.println("Signature: " + signature);
```

### 8. Database Schema Issues

**Error:** `Table doesn't exist`

**Solution:**

```yaml
# Force recreate tables (WARNING: data loss)
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop  # For dev only!
```

```bash
# Manual schema creation
mysql -h localhost -P 3333 -u admin -padmin smart-bin < schema.sql
```

---

## 📚 Tài Liệu Tham Khảo

### Official Documentation

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security OAuth2](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Spring Data Redis](https://spring.io/projects/spring-data-redis)

### Third-party Services

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Keycloak Admin REST API](https://www.keycloak.org/docs-api/latest/rest-api/)
- [MinIO Java SDK](https://min.io/docs/minio/linux/developers/java/minio-java.html)
- [Redis Documentation](https://redis.io/docs/)

### Libraries

- [Lombok](https://projectlombok.org/)
- [MapStruct](https://mapstruct.org/)
- [OkHttp](https://square.github.io/okhttp/)
- [Apache Tika](https://tika.apache.org/)

---

## 👥 Contributors

- **Backend Development** - Bảo Kiều
- **System Architecture** - Smart Bin Team
- **Documentation** - Smart Bin Team

---

## 📝 License

This project is part of HUST SOICT Project 3.

---

<div align="center">
  <p>Made with ❤️ for Smart Bin System</p>
  <p>HUST - SOICT - Project 3 - 2026</p>
</div>
