# OAuth2 Debug Guide - Khắc phục lỗi IllegalArgumentException và EMAIL_NOT_PROVIDED

## Tóm tắt vấn đề
- **Lỗi chính**: `IllegalArgumentException: Attribute value for 'id' cannot be null`
- **Lỗi phụ**: `EMAIL_NOT_PROVIDED` 
- **Lỗi Unicode**: `Unicode character cannot be encoded` - URL redirect chứa ký tự tiếng Việt
- **Lỗi Session**: `authorization_request_not_found` - Session bị mất khi xử lý callback
- **Nguyên nhân**: DefaultOAuth2User/DefaultOidcUser được tạo với attribute `id` có giá trị null

## Các thay đổi đã thực hiện

### 1. Vô hiệu hóa OIDC và chỉ sử dụng OAuth2
- ✅ Xóa scope `openid` khỏi Google OAuth2 configuration
- ✅ Đặt `user-name-attribute=id` cho Google (thay vì `sub`)
- ✅ Comment out `oidcUserService` trong SecurityConfig

### 2. Tạo GoogleOAuth2UserService chuyên biệt
- ✅ Tạo `GoogleOAuth2UserService` để xử lý riêng Google OAuth2
- ✅ Bypass Spring Security default OAuth2 user loading
- ✅ Xử lý trực tiếp Google OAuth2 responses

### 3. Thêm logging chi tiết
- ✅ Thêm debug logging vào tất cả methods trong `FlexibleOAuth2UserService`
- ✅ Cấu hình logging level = DEBUG cho package `com.example.online_quiz_system`

### 4. Khắc phục lỗi Unicode encoding
- ✅ Cấu hình UTF-8 encoding cho Tomcat
- ✅ Cập nhật OAuth2AuthenticationSuccessHandler để encode URL đúng cách
- ✅ Xử lý ký tự tiếng Việt trong redirect URL

### 5. Khắc phục lỗi authorization_request_not_found
- ✅ Cấu hình session management tốt hơn
- ✅ Tăng session timeout và max sessions
- ✅ Cập nhật InMemoryOAuth2AuthorizationRequestRepository với logging chi tiết

### 6. Cải tiến nâng cao (ADVANCED FIXES)
- ✅ Enhanced Unicode handling với multiple fallback strategies
- ✅ Advanced session management với multiple storage keys
- ✅ Enhanced provider detection và validation
- ✅ Improved error handling và logging

### 7. Sửa lỗi nhỏ sau khi OAuth2 hoạt động
- ✅ Sửa LazyInitializationException trong OAuth2AuthenticationSuccessHandler
- ✅ Sửa provider hiển thị "unknown" với method extractProvider
- ✅ Sửa URL encoding bị double-encode
- ✅ Cải tiến error handling và fallback mechanisms

### 8. Sửa triệt để 2 vấn đề cuối cùng
- ✅ Sửa triệt để lỗi Unicode trong Redirect URL - Vietnamese character mapping
- ✅ Sửa triệt để lỗi Authorization Request Not Found - Enhanced session management
- ✅ Tăng session timeout và expiry time
- ✅ Session-based fallback mechanism

### 9. Cấu hình OAuth2
```properties
# Google OAuth2 (OIDC disabled)
spring.security.oauth2.client.registration.google.scope=profile,email
spring.security.oauth2.client.provider.google.user-name-attribute=id

# UTF-8 Encoding Configuration
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true
server.tomcat.uri-encoding=UTF-8

# Session Configuration for OAuth2
server.servlet.session.timeout=30m
server.servlet.session.cookie.max-age=1800
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.secure=false
```

## Hướng dẫn test và debug

### Bước 1: Khởi động ứng dụng
```bash
cd backend/online-quiz-system
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Bước 2: Test OAuth2 authentication
1. Truy cập: `http://localhost:8080/login`
2. Click "Login with Google"
3. Thực hiện OAuth2 flow với Google

### Bước 3: Quan sát logs
Tìm các log messages sau trong console:

#### Logs quan trọng cần chú ý:
```
DEBUG c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Loading OAuth2 user for provider: google
DEBUG c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Raw Google attributes: {...}
DEBUG c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Google ID: ...
DEBUG c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Google email: ...
INFO c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Google OAuth2 user processed successfully - Provider: google, ID: ..., Name: ..., Email: ...
```

#### Logs nâng cao (ADVANCED FIXES):
```
DEBUG c.e.o.s.InMemoryOAuth2AuthorizationRequestRepository - Saving OAuth2 authorization request for state: ... (storage size: ...)
DEBUG c.e.o.s.InMemoryOAuth2AuthorizationRequestRepository - Also saved OAuth2 authorization request with session ID: ...
DEBUG c.e.o.s.OAuth2AuthenticationSuccessHandler - Building success URL with proper encoding
DEBUG c.e.o.s.OAuth2AuthenticationSuccessHandler - Sanitizing Unicode string: ...
```

#### Logs lỗi cần chú ý:
```
ERROR c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Google response missing 'id' field
ERROR c.e.o.s.GoogleOAuth2UserService - GoogleOAuth2UserService: Google response missing 'email' field
WARN c.e.o.s.InMemoryOAuth2AuthorizationRequestRepository - No OAuth2 authorization request found for state: ...
WARN c.e.o.s.OAuth2AuthenticationSuccessHandler - Failed to encode string: ...
```

### Bước 4: Phân tích kết quả

#### Nếu thành công:
- Sẽ thấy log: `GoogleOAuth2UserService: Google OAuth2 user processed successfully - Provider: google, ID: ..., Name: ..., Email: ...`
- Sẽ thấy log: `Building success URL with proper encoding`
- Sẽ thấy log: `Sanitizing Unicode string: ...`
- User được redirect về frontend với URL được encode đúng cách

#### Nếu vẫn lỗi:
- Kiểm tra attributes được trả về từ Google
- Xem ID field có giá trị gì
- Xem email field có giá trị gì
- Chụp screenshot logs và gửi để phân tích

## Các trường hợp có thể xảy ra

### 1. Google không trả về email
**Triệu chứng**: Log `GoogleOAuth2UserService: Google response missing 'email' field`
**Nguyên nhân**: Google OAuth2 app chưa được cấu hình để request email scope
**Giải pháp**: Kiểm tra Google Console OAuth2 app settings

### 2. ID field bị null
**Triệu chứng**: Log `GoogleOAuth2UserService: Google response missing 'id' field`
**Nguyên nhân**: Google không trả về field `id` (rất hiếm)
**Giải pháp**: Kiểm tra Google Console OAuth2 app settings

### 3. Attributes không đúng format
**Triệu chứng**: Log `GoogleOAuth2UserService: Raw Google attributes: {...}`
**Nguyên nhân**: Google trả về attributes khác với expected format
**Giải pháp**: Điều chỉnh logic xử lý attributes trong GoogleOAuth2UserService

### 4. Lỗi Unicode encoding
**Triệu chứng**: `Unicode character cannot be encoded` hoặc ký tự tiếng Việt bị lỗi
**Nguyên nhân**: Tomcat không hỗ trợ UTF-8 encoding mặc định
**Giải pháp**: Đã cấu hình UTF-8 encoding trong application.properties

### 5. Lỗi authorization_request_not_found
**Triệu chứng**: Log `No OAuth2 authorization request found for state`
**Nguyên nhân**: Session bị mất giữa authorization request và callback
**Giải pháp**: Đã cấu hình session management và tăng session timeout

## Debug commands

### Xem tất cả logs liên quan đến OAuth2:
```bash
grep -i "oauth2\|flexible" logs/application.log
```

### Xem logs của GoogleOAuth2UserService:
```bash
grep "GoogleOAuth2UserService" logs/application.log
```

### Xem logs lỗi:
```bash
grep -i "error\|exception" logs/application.log
```

## Kết quả mong đợi

Sau khi test, bạn sẽ thấy logs chi tiết cho biết:
1. Google trả về attributes gì
2. ID field có giá trị gì
3. Email field có giá trị gì
4. Tại sao có thể xảy ra lỗi

Dựa trên kết quả logs, chúng ta sẽ có thể xác định chính xác nguyên nhân và sửa lỗi.

## Liên hệ nếu cần hỗ trợ

Nếu vẫn gặp vấn đề, hãy:
1. Chụp screenshot logs
2. Copy toàn bộ logs liên quan đến OAuth2
3. Gửi kèm thông tin lỗi cụ thể

Chúng ta sẽ phân tích logs để tìm ra giải pháp cuối cùng.
