# Facebook Domain Error Fix

## ❌ Lỗi gặp phải:
```
oauth JS SDK redirect domains[0] should represent a valid URL
```

## 🔍 Nguyên nhân:
Facebook yêu cầu **App Domains** phải là **URL đầy đủ** (bao gồm protocol), không chỉ là domain.

## ✅ Giải pháp:

### 1. Thay đổi App Domains

**❌ SAI:**
```
localhost
```

**✅ ĐÚNG:**
```
http://localhost:3000
http://localhost:8080
```

### 2. Cấu hình chi tiết trong Facebook Console

#### **Bước 1: Vào Settings > Basic > App Domains**
Thêm từng URL một:
```
http://localhost:3000
http://localhost:8080
```

#### **Bước 2: Vào Products > Website > Settings**
```
Site URL: http://localhost:3000
```

#### **Bước 3: Vào Products > Facebook Login > Settings > Valid OAuth Redirect URIs**
```
http://localhost:8080/login/oauth2/code/facebook
http://localhost:3000/oauth2/success
http://localhost:3000/oauth2/error
```

### 3. Giải thích từng URL:

- **`http://localhost:3000`** - Frontend React app
- **`http://localhost:8080`** - Backend Spring Boot app
- **`http://localhost:8080/login/oauth2/code/facebook`** - OAuth2 callback URI
- **`http://localhost:3000/oauth2/success`** - Success redirect page
- **`http://localhost:3000/oauth2/error`** - Error redirect page

### 4. Kiểm tra sau khi sửa:

1. **Lưu cấu hình** trong Facebook Console
2. **Đợi 5-10 phút** để Facebook cập nhật
3. **Test Facebook login:**
   - URL: http://localhost:8080/oauth2/authorization/facebook
   - Kiểm tra không còn lỗi domain

### 5. Production Configuration:

Khi deploy lên production:

```
App Domains:
- https://yourdomain.com
- https://www.yourdomain.com

Redirect URIs:
- https://yourdomain.com/login/oauth2/code/facebook
- https://yourdomain.com/oauth2/success
- https://yourdomain.com/oauth2/error
```

## 🎯 Kết quả mong đợi:

- ✅ Không còn lỗi "oauth JS SDK redirect domains[0] should represent a valid URL"
- ✅ Facebook login hoạt động bình thường
- ✅ OAuth2 redirect flow hoạt động đúng
- ✅ JWT token được tạo thành công

## 📞 Troubleshooting:

### Nếu vẫn còn lỗi:
1. **Kiểm tra URL format** - Phải có http:// hoặc https://
2. **Đợi cache refresh** - Facebook cần thời gian cập nhật
3. **Clear browser cache** - Xóa cache trình duyệt
4. **Kiểm tra App status** - Đảm bảo App ở chế độ Development

### Debug steps:
1. Kiểm tra Facebook Console settings
2. Test với URL trực tiếp
3. Xem browser console logs
4. Kiểm tra Spring Boot logs
