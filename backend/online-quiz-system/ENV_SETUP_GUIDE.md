# 🔧 HƯỚNG DẪN THIẾT LẬP BIẾN MÔI TRƯỜNG

## ❌ VẤN ĐỀ HIỆN TẠI

Spring Boot không thể khởi tạo bean `OAuth2Configuration` vì:
- File cấu hình sử dụng `${GOOGLE_CLIENT_ID}` và `${GOOGLE_CLIENT_SECRET}`
- Biến môi trường `GOOGLE_CLIENT_ID` không tồn tại
- Không có file `.env` để cung cấp các biến môi trường

## ✅ GIẢI PHÁP

### Bước 1: Tạo file `.env` trong thư mục `backend/online-quiz-system/`

```bash
# Tạo file .env
touch .env
```

### Bước 2: Thêm nội dung vào file `.env`

```env
# =============================================
# ENVIRONMENT VARIABLES - REQUIRED!
# =============================================

# =============================================
# SPRING PROFILE
# =============================================
SPRING_PROFILES_ACTIVE=dev

# =============================================
# DATABASE CONFIGURATION
# =============================================
DB_URL=jdbc:postgresql://localhost:5432/quizdb
DB_USERNAME=quizuser
DB_PASSWORD=quizpass

# =============================================
# MAIL CONFIGURATION
# =============================================
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
MAIL_DEBUG=false

# =============================================
# JWT CONFIGURATION
# =============================================
JWT_SECRET=mySecretKey123456789012345678901234567890123456789012345678901234567890
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# =============================================
# OAUTH2 CONFIGURATION - QUAN TRỌNG!
# =============================================
# Bạn PHẢI thay thế các giá trị này bằng thông tin thực từ Google Cloud Console
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret

# Bạn PHẢI thay thế các giá trị này bằng thông tin thực từ Facebook Developers
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret

# =============================================
# FRONTEND CONFIGURATION
# =============================================
FRONTEND_URL=http://localhost:3000
FRONTEND_ORIGIN=http://localhost:3000
CORS_ORIGINS=http://localhost:3000

# =============================================
# SERVER CONFIGURATION
# =============================================
SERVER_PORT=8080

# =============================================
# LOGGING CONFIGURATION
# =============================================
LOG_LEVEL=INFO
OAUTH2_LOG_LEVEL=DEBUG
REST_LOG_LEVEL=INFO
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true

# =============================================
# PERFORMANCE CONFIGURATION
# =============================================
DB_POOL_SIZE=20
DB_POOL_MIN=5
DB_IDLE_TIMEOUT=300000
DB_MAX_LIFETIME=1800000
DB_CONNECTION_TIMEOUT=20000
DB_LEAK_DETECTION=60000
JPA_BATCH_SIZE=20
JPA_STATS=false

# =============================================
# REDIS CONFIGURATION
# =============================================
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=
REDIS_TIMEOUT=2000ms
REDIS_POOL_MAX_ACTIVE=8
REDIS_POOL_MAX_IDLE=8
REDIS_POOL_MIN_IDLE=0
CACHE_TTL=600000
```

### Bước 3: Lấy thông tin OAuth2 từ Google Cloud Console

1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Kích hoạt Google+ API
4. Tạo OAuth 2.0 Client ID:
   - Application type: Web application
   - Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`
5. Copy `Client ID` và `Client Secret`

### Bước 4: Lấy thông tin OAuth2 từ Facebook Developers

1. Truy cập [Facebook Developers](https://developers.facebook.com/)
2. Tạo app mới
3. Thêm Facebook Login product
4. Cấu hình Valid OAuth Redirect URIs: `http://localhost:8080/login/oauth2/code/facebook`
5. Copy `App ID` và `App Secret`

### Bước 5: Cập nhật file `.env`

Thay thế các giá trị placeholder bằng thông tin thực:

```env
GOOGLE_CLIENT_ID=123456789-abcdefghijklmnop.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=GOCSPX-abcdefghijklmnopqrstuvwxyz
FACEBOOK_CLIENT_ID=1234567890123456
FACEBOOK_CLIENT_SECRET=abcdefghijklmnopqrstuvwxyz123456
```

### Bước 6: Kiểm tra cấu hình

```bash
# Kiểm tra biến môi trường
echo $GOOGLE_CLIENT_ID
echo $GOOGLE_CLIENT_SECRET
```

## 🚨 LƯU Ý QUAN TRỌNG

1. **KHÔNG commit file `.env`** vào Git
2. **File `.env` đã được thêm vào `.gitignore`**
3. **Phải có thông tin OAuth2 thực** để ứng dụng hoạt động
4. **Kiểm tra lại cấu hình** trước khi chạy ứng dụng

## 🔍 KIỂM TRA LỖI

Nếu vẫn gặp lỗi, kiểm tra:

1. File `.env` có tồn tại không?
2. Biến `GOOGLE_CLIENT_ID` có giá trị không?
3. Spring Boot có đọc được file `.env` không?
4. Cấu hình OAuth2 có đúng format không?

## 📝 LOGS ĐỂ DEBUG

Thêm vào `application-dev.properties`:

```properties
logging.level.org.springframework.security.oauth2=DEBUG
logging.level.com.example.online_quiz_system=DEBUG
```

Sau đó chạy ứng dụng và xem logs để debug.
