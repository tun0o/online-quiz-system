# Facebook OAuth2 Setup Guide

## 📋 Thông tin Facebook App

- **App ID:** 1451200836151168
- **App Secret:** a785877528258f0130c717003e50636a
- **Redirect URI:** http://localhost:8080/login/oauth2/code/facebook

## 🔧 Cấu hình đã hoàn thành

### 1. Application Properties
```properties
# Facebook OAuth2 Configuration
spring.security.oauth2.client.registration.facebook.client-id=1451200836151168
spring.security.oauth2.client.registration.facebook.client-secret=a785877528258f0130c717003e50636a
spring.security.oauth2.client.registration.facebook.scope=email,public_profile
spring.security.oauth2.client.registration.facebook.redirect-uri=http://localhost:8080/login/oauth2/code/facebook

spring.security.oauth2.client.provider.facebook.authorization-uri=https://www.facebook.com/v18.0/dialog/oauth
spring.security.oauth2.client.provider.facebook.token-uri=https://graph.facebook.com/v18.0/oauth/access_token
spring.security.oauth2.client.provider.facebook.user-info-uri=https://graph.facebook.com/v18.0/me?fields=id,name,email,picture
spring.security.oauth2.client.provider.facebook.user-name-attribute=id
```

### 2. MultiProviderOAuth2UserService
- ✅ Tạo service tổng quát xử lý cả Google và Facebook
- ✅ Xử lý Facebook attributes đặc biệt (picture object)
- ✅ Validation và fallback mechanisms
- ✅ Logging chi tiết cho debugging

### 3. SecurityConfig
- ✅ Cập nhật để sử dụng MultiProviderOAuth2UserService
- ✅ Hỗ trợ cả Google và Facebook OAuth2

## 🚀 Test Facebook OAuth2

### 1. Khởi động ứng dụng
```bash
cd backend/online-quiz-system
mvn spring-boot:run
```

### 2. Test URLs
- **Facebook Login:** http://localhost:8080/oauth2/authorization/facebook
- **Success Page:** http://localhost:3000/oauth2/success
- **Error Page:** http://localhost:3000/oauth2/error

### 3. Expected Flow
1. User clicks Facebook login button
2. Redirect to Facebook OAuth2 authorization
3. User authorizes app on Facebook
4. Facebook redirects back to: `http://localhost:8080/login/oauth2/code/facebook`
5. Spring Security processes OAuth2 callback
6. MultiProviderOAuth2UserService processes Facebook user data
7. OAuth2AuthenticationSuccessHandler generates JWT
8. Redirect to frontend with authentication data

## 🔍 Debugging

### 1. Check Logs
```bash
# Enable debug logging for OAuth2
logging.level.com.example.online_quiz_system=DEBUG
logging.level.org.springframework.security.oauth2=DEBUG
```

### 2. Common Issues
- **Invalid redirect URI:** Đảm bảo Facebook App có redirect URI chính xác
- **Scope permissions:** Đảm bảo Facebook App có quyền email và public_profile
- **App review:** Facebook App có thể cần review cho production

### 3. Facebook App Settings
1. Vào Facebook Developers Console
2. Chọn App ID: 1451200836151168
3. Settings > Basic > App Domains: localhost
4. Products > Facebook Login > Settings > Valid OAuth Redirect URIs:
   - http://localhost:8080/login/oauth2/code/facebook
   - http://localhost:3000/oauth2/success

## 📱 Frontend Integration

### 1. Facebook Login Button
```jsx
<a href="http://localhost:8080/oauth2/authorization/facebook" 
   className="btn btn-primary">
   <i className="fab fa-facebook"></i> Đăng nhập với Facebook
</a>
```

### 2. Handle OAuth2 Success
```javascript
// Check for OAuth2 success parameters
const urlParams = new URLSearchParams(window.location.search);
if (urlParams.get('token')) {
    // Store JWT token
    localStorage.setItem('token', urlParams.get('token'));
    // Redirect to dashboard
    window.location.href = '/dashboard';
}
```

## ✅ Checklist

- [x] Facebook App ID và Secret đã được cập nhật
- [x] Application properties đã được cấu hình
- [x] MultiProviderOAuth2UserService đã được tạo
- [x] SecurityConfig đã được cập nhật
- [x] FacebookOAuth2UserInfo đã có sẵn
- [x] OAuth2UserInfoFactory hỗ trợ Facebook
- [x] Compilation thành công
- [ ] Test Facebook login flow
- [ ] Verify JWT generation
- [ ] Test frontend integration

## 🎯 Next Steps

1. **Test Facebook OAuth2 flow** - Đảm bảo login hoạt động
2. **Verify user data** - Kiểm tra thông tin user từ Facebook
3. **Test JWT generation** - Đảm bảo token được tạo đúng
4. **Frontend integration** - Tích hợp với React frontend
5. **Error handling** - Test các trường hợp lỗi

## 📞 Support

Nếu gặp vấn đề:
1. Kiểm tra Facebook App settings
2. Xem logs để debug
3. Đảm bảo redirect URIs chính xác
4. Test với Facebook App trong development mode
