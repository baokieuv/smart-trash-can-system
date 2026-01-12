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

---

## 🚀 Công nghệ

| Technology | Version | Purpose |
|------------|---------|---------|
| **Java** | 17+ | Programming language |
| **Spring Boot** | 3.x | Framework |
| **Spring Security** | 6.x | OAuth2 Resource Server |
| **Spring Data JPA** | 3.x | Database ORM |
| **Hibernate** | 6.x | JPA implementation |
| **MariaDB** | 10.6+ | Database |
| **Keycloak Java Client** | 23.x | Keycloak admin API |
| **JavaMail** | - | Email sending |
| **OkHttp** | 4.x | HTTP client (FastAPI calls) |
| **Gson** | 2.x | JSON parsing |
| **Lombok** | - | Boilerplate reduction |

---

## 📱 Yêu cầu

- **Java**: JDK 17+
- **Maven**: 3.8+
- **MariaDB**: 10.6+
- **Keycloak**: 23.x+ (running)
- **FastAPI**: Running on port 8000
- **SMTP Server**: Gmail/Mailgun/etc

---

## 🛠️ Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/your-username/smart-bin-system.git
cd smart-bin-system/backend-springboot
```

### 2. Install dependencies

```bash
./mvnw clean install
# Windows: mvnw.cmd clean install
```

### 3. Setup MariaDB

```bash
# Docker
docker run -d \
  --name mariadb \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=smart_bin_db \
  mariadb:10.6

# Hoặc cài local
mysql -u root -p
CREATE DATABASE smart_bin_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4. Setup Keycloak

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
1. Login: http://localhost:8081 (admin/admin)
2. Create realm: `smart-bin-realm`
3. Create client: `smart-bin-client`
   - Client authentication: ON
   - Standard flow: ON
   - Direct access grants: ON
   - Service accounts roles: ON
4. Copy Client Secret từ Credentials tab

---

## ⚙️ Cấu hình

### application.properties

File: `src/main/resources/application.properties`

```properties
# ========== Server ==========
server.port=8080

# ========== Database ==========
spring.datasource.url=jdbc:mariadb://localhost:3306/smart_bin_db?useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=your_password
spring.datasource.driver-class-name=org.mariadb.jdbc.Driver

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MariaDBDialect
spring.jpa.properties.hibernate.format_sql=true

# ========== Keycloak Admin Client ==========
keycloak.server-url=http://localhost:8081
keycloak.realm=smart-bin-realm
keycloak.client-id=smart-bin-client
keycloak.client-secret=YOUR_CLIENT_SECRET_FROM_KEYCLOAK
keycloak.admin-username=admin
keycloak.admin-password=admin

# ========== OAuth2 Resource Server ==========
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8081/realms/smart-bin-realm
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8081/realms/smart-bin-realm/protocol/openid-connect/certs

# ========== Email Configuration ==========
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your_email@gmail.com
spring.mail.password=your_app_password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true

# Email settings
app.email.from=noreply@smartbin.com
app.email.verification.url=http://localhost:3000/verify-email

# ========== FastAPI AI Server ==========
app.ai-server.url=http://localhost:8000

# ========== Logging ==========
logging.level.root=INFO
logging.level.com.example.smart_bin_server=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### Environment Variables (Optional)

Thay vì hardcode trong `application.properties`, có thể dùng env vars:

```bash
export DB_URL=jdbc:mariadb://localhost:3306/smart_bin_db
export DB_USERNAME=root
export DB_PASSWORD=your_password
export KEYCLOAK_SERVER_URL=http://localhost:8081
export KEYCLOAK_CLIENT_SECRET=your_secret
export SMTP_USERNAME=your_email@gmail.com
export SMTP_PASSWORD=your_app_password
```

Sau đó trong `application.properties`:

```properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
keycloak.client-secret=${KEYCLOAK_CLIENT_SECRET}
spring.mail.username=${SMTP_USERNAME}
spring.mail.password=${SMTP_PASSWORD}
```

---

## 📂 Cấu trúc code

```
src/main/java/com/example/smart_bin_server/
│
├── config/                         # Configuration classes
│   ├── Constants.java             # App constants
│   ├── KeycloakConfig.java        # Keycloak admin client
│   ├── SecurityConfig.java        # Spring Security & OAuth2
│   └── RedisConfig.java           # Redis (nếu dùng)
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
│   ├── ClassificationService.java # Call FastAPI
│   └── RefreshTokenService.java   # (Deprecated - dùng Keycloak)
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

Chi tiết xem [API Documentation](../README.md#-api-documentation) ở root README.

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

### KeycloakConfig.java

Tạo Keycloak admin client để quản lý users.

```java
@Bean
public Keycloak keycloak() {
    return KeycloakBuilder.builder()
            .serverUrl(serverUrl)
            .realm("master")
            .grantType(OAuth2Constants.PASSWORD)
            .clientId("admin-cli")
            .username(adminUsername)
            .password(adminPassword)
            .build();
}
```

### KeycloakService.java

Các method chính:

```java
// Tạo user trong Keycloak (disabled)
public String createUser(RegisterRequest request)

// Enable user sau khi verify email
public void enableUser(String userId)

// Login → lấy access + refresh token từ Keycloak
public TokenResponse login(LoginRequest request)

// Refresh access token
public TokenResponse refreshAccessToken(String refreshToken)

// Logout → revoke token
public void logout(String refreshToken)

// Đổi password
public void updatePassword(String userId, String newPassword)

// Lấy user từ Keycloak
public UserRepresentation getUserById(String userId)
public UserRepresentation getUserByEmail(String email)
```

### SecurityConfig.java

Cấu hình Spring Security:

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/v1/auth/**").permitAll()
            .requestMatchers("/api/v1/classify-image/**").permitAll()
            .requestMatchers("/api/v1/devices/**").authenticated()
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt
                .jwtAuthenticationConverter(jwtAuthenticationConverter())
            )
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        );
    
    return http.build();
}
```

---

## 📧 Email Service

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

### DeviceService.java

Check device status mỗi 10 giây:

```java
@Scheduled(fixedRate = 10000)
public void checkDevicesStatus() {
    List<Device> onlineDevices = repository.findByStatus("ONLINE");
    long now = System.currentTimeMillis();
    
    for (Device device : onlineDevices) {
        DeviceData data = dataRepository.findById(device.getId()).orElse(null);
        
        // Nếu không nhận data trong 60s → set OFFLINE
        if (data != null && now - data.getTimestamp() > 60000) {
            device.setStatus("OFFLINE");
            
            Notification notification = new Notification();
            notification.setDeviceId(device.getId());
            notification.setType("WARNING");
            notification.setMessage("Device disconnected.");
            notificationService.addNotification(notification);
        }
    }
    
    repository.saveAll(onlineDevices);
}
```

---

## 🚀 Chạy ứng dụng

### Development Mode

```bash
./mvnw spring-boot:run

# With profile
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

### Production Mode

```bash
# Build JAR
./mvnw clean package -DskipTests

# Run JAR
java -jar target/smart-bin-server-0.0.1-SNAPSHOT.jar

# With env vars
export SPRING_PROFILES_ACTIVE=prod
export DB_PASSWORD=secure_password
java -jar target/smart-bin-server-0.0.1-SNAPSHOT.jar
```

### Docker

```dockerfile
FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

Build & Run:

```bash
docker build -t smart-bin-backend .
docker run -p 8080:8080 \
  -e DB_URL=jdbc:mariadb://host.docker.internal:3306/smart_bin_db \
  -e DB_PASSWORD=your_password \
  smart-bin-backend
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