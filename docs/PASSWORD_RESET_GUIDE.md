# Hướng dẫn sử dụng tính năng Quên mật khẩu và Đổi mật khẩu

## 🔐 **TÍNH NĂNG QUÊN MẬT KHẨU**

### **Cách sử dụng:**
1. Truy cập trang đăng nhập
2. Nhấn "Quên mật khẩu?"
3. Nhập email đã đăng ký
4. Kiểm tra email và nhấn vào liên kết
5. Nhập mật khẩu mới
6. Đăng nhập với mật khẩu mới

### **API Endpoints:**
- `POST /api/auth/forgot-password` - Gửi email reset
- `POST /api/auth/reset-password` - Đặt lại mật khẩu

### **Bảo mật:**
- Token có hiệu lực 1 giờ
- Rate limiting: 5 phút giữa các lần gửi
- Token được hash bằng SHA-256
- Tự động dọn dẹp token hết hạn

---

## 🔄 **TÍNH NĂNG ĐỔI MẬT KHẨU**

### **Cách sử dụng:**
1. Đăng nhập vào tài khoản
2. Truy cập `/change-password`
3. Nhập mật khẩu hiện tại
4. Nhập mật khẩu mới (đáp ứng yêu cầu bảo mật)
5. Xác nhận mật khẩu mới
6. Nhấn "Đổi mật khẩu"

### **API Endpoint:**
- `POST /api/auth/change-password` - Đổi mật khẩu

### **Yêu cầu mật khẩu:**
- Tối thiểu 8 ký tự
- Có chữ hoa và chữ thường
- Có số và ký tự đặc biệt

---

## 👨‍💼 **TÍNH NĂNG CHUYỂN ĐỔI GIAO DIỆN ADMIN**

### **Cách sử dụng:**
1. Đăng nhập với tài khoản ADMIN
2. Trong Admin Panel, nhấn "Xem giao diện User"
3. ADMIN có thể trải nghiệm giao diện người dùng thường
4. Nhấn "Quay lại Admin" để trở về giao diện quản trị

### **Tính năng trong User View:**
- Xem thông tin cá nhân
- Quản lý bài nộp
- Tạo bài nộp mới
- Xem thống kê hoạt động
- Đổi mật khẩu

---

## 🛠️ **CẤU HÌNH EMAIL**

Đảm bảo cấu hình email trong `application.properties`:

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 🔧 **TROUBLESHOOTING**

### **Lỗi thường gặp:**

1. **Email không được gửi:**
   - Kiểm tra cấu hình SMTP
   - Kiểm tra firewall/antivirus
   - Kiểm tra email trong spam

2. **Token không hợp lệ:**
   - Token đã hết hạn (1 giờ)
   - Token đã được sử dụng
   - URL không đúng format

3. **Mật khẩu không đáp ứng yêu cầu:**
   - Kiểm tra độ dài tối thiểu 8 ký tự
   - Đảm bảo có đủ loại ký tự (hoa, thường, số, đặc biệt)

---

## 📱 **FRONTEND ROUTES**

### **Public Routes:**
- `/login` - Đăng nhập
- `/register` - Đăng ký
- `/forgot-password` - Quên mật khẩu
- `/reset-password` - Đặt lại mật khẩu

### **Protected Routes:**
- `/change-password` - Đổi mật khẩu
- `/admin/user-view` - Giao diện User cho ADMIN

---

## 🔒 **BẢO MẬT**

### **Các biện pháp bảo mật:**
- Token được hash trước khi lưu database
- Rate limiting cho việc gửi email
- Validation mật khẩu mạnh
- Tự động dọn dẹp token hết hạn
- Kiểm tra quyền truy cập cho từng endpoint

### **Best Practices:**
- Không log token trong production
- Sử dụng HTTPS cho tất cả requests
- Validate input từ phía client và server
- Rate limiting cho API endpoints

