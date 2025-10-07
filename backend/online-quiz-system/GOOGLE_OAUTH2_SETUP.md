# Google OAuth2 Setup Guide

## ❌ Lỗi hiện tại: "Error 401: invalid_client - The OAuth client was not found"

### 🔍 Nguyên nhân:
- Client ID hiện tại là placeholder: `your-google-client-id`
- Chưa có Google OAuth2 Client thật

### 🔧 Cách sửa lỗi:

#### Bước 1: Tạo Google Cloud Project
1. Truy cập [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project mới hoặc chọn project hiện có
3. Đặt tên project: "Online Quiz System"

#### Bước 2: Enable Google+ API
1. Vào **APIs & Services** → **Library**
2. Tìm "Google+ API" hoặc "Google People API"
3. Click **Enable**

#### Bước 3: Tạo OAuth2 Credentials
1. Vào **APIs & Services** → **Credentials**
2. Click **Create Credentials** → **OAuth 2.0 Client IDs**
3. Chọn **Web application**
4. Cấu hình:
   - **Name:** Online Quiz System
   - **Authorized JavaScript origins:** `http://localhost:8080`
   - **Authorized redirect URIs:** `http://localhost:8080/login/oauth2/code/google`

#### Bước 4: Lấy Client ID và Secret
Sau khi tạo, bạn sẽ nhận được:
- **Client ID:** `123456789-abcdefg.apps.googleusercontent.com`
- **Client Secret:** `GOCSPX-abcdefghijklmnop`

#### Bước 5: Cập nhật cấu hình
Thay thế trong `application.properties`:

```properties
# OAuth2 Configuration
spring.security.oauth2.client.registration.google.client-id=YOUR_ACTUAL_GOOGLE_CLIENT_ID
spring.security.oauth2.client.registration.google.client-secret=YOUR_ACTUAL_GOOGLE_CLIENT_SECRET
```

#### Bước 6: Restart Application
```bash
# Dừng application
# Khởi động lại
java -jar target/online-quiz-system-0.0.1-SNAPSHOT.jar
```

### 🧪 Test OAuth2:
1. Truy cập: `http://localhost:8080/oauth2/authorization/google`
2. Sẽ redirect đến Google login
3. Sau khi login, sẽ redirect về: `http://localhost:8080/login/oauth2/code/google`

### ⚠️ Lưu ý:
- **Client ID** phải là thật, không phải placeholder
- **Redirect URI** phải khớp chính xác
- **Google+ API** phải được enable
- **OAuth consent screen** phải được cấu hình

### 🔗 Links hữu ích:
- [Google Cloud Console](https://console.cloud.google.com/)
- [OAuth2 Setup Guide](https://developers.google.com/identity/protocols/oauth2)
- [Spring Security OAuth2](https://spring.io/guides/tutorials/spring-boot-oauth2/)
