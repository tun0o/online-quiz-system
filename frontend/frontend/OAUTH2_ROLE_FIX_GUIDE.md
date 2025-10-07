# 🔧 OAuth2 Role Fix Guide

## ✅ **Đã sửa xong:**

### **1. OAuth2Success.jsx - Dynamic Role Loading**
```javascript
// ✅ BEFORE (Hardcoded):
roles: ['ROLE_USER'], // OAuth2 users default to USER role

// ✅ AFTER (Dynamic):
const userFromToken = getUserFromToken(token);
const roles = userFromToken?.roles || ['ROLE_USER'];
```

### **2. Smart Redirect Based on Role**
```javascript
// ✅ BEFORE (Always user dashboard):
navigate('/user/dashboard', { replace: true });

// ✅ AFTER (Role-based redirect):
const redirectPath = roles.includes('ROLE_ADMIN') ? '/admin/dashboard' : '/user/dashboard';
navigate(redirectPath, { replace: true });
```

### **3. Display Role in UI**
```javascript
// ✅ Added role display in success page:
<span className="text-gray-600">Quyền hạn:</span>
<span className="ml-2 font-medium">
    {userData.roles?.includes('ROLE_ADMIN') ? 'Quản trị viên' : 'Người dùng'}
</span>
```

---

## 🧪 **Cách test:**

### **1. Test với user có role USER:**
1. Đăng nhập bằng Facebook với user thường
2. Kiểm tra redirect đến `/user/dashboard`
3. Kiểm tra hiển thị "Người dùng" trong UI

### **2. Test với user có role ADMIN:**
1. Đổi role user trong database:
```sql
UPDATE users SET role = 'ADMIN' WHERE email = 'user@example.com';
```
2. User đăng xuất và đăng nhập lại
3. Kiểm tra redirect đến `/admin/dashboard`
4. Kiểm tra hiển thị "Quản trị viên" trong UI

### **3. Test JWT Token Parsing:**
```javascript
// Debug trong browser console:
const token = localStorage.getItem('accessToken');
const userFromToken = getUserFromToken(token);
console.log('Roles from token:', userFromToken?.roles);
```

---

## 🔍 **Debug Steps:**

### **1. Kiểm tra JWT Token:**
```javascript
// Trong browser console:
const token = localStorage.getItem('accessToken');
console.log('JWT Token:', token);

// Decode token manually:
const payload = token.split('.')[1];
const decoded = JSON.parse(atob(payload));
console.log('Decoded payload:', decoded);
console.log('Roles in token:', decoded.roles);
```

### **2. Kiểm tra User Data:**
```javascript
// Trong browser console:
const user = JSON.parse(localStorage.getItem('user'));
console.log('Stored user data:', user);
console.log('User roles:', user.roles);
```

### **3. Kiểm tra Redirect Logic:**
```javascript
// Trong browser console:
const roles = ['ROLE_ADMIN']; // hoặc ['ROLE_USER']
const redirectPath = roles.includes('ROLE_ADMIN') ? '/admin/dashboard' : '/user/dashboard';
console.log('Redirect path:', redirectPath);
```

---

## 🎯 **Kết quả mong đợi:**

### **✅ User với role USER:**
- Redirect đến `/user/dashboard`
- Hiển thị "Người dùng" trong UI
- Có quyền truy cập user endpoints

### **✅ User với role ADMIN:**
- Redirect đến `/admin/dashboard`
- Hiển thị "Quản trị viên" trong UI
- Có quyền truy cập admin endpoints

### **✅ Fallback:**
- Nếu không parse được role từ JWT → default `ROLE_USER`
- Nếu JWT token invalid → redirect đến login

---

## 🚀 **Deployment:**

1. **Build frontend:**
```bash
cd frontend/frontend
npm run build
```

2. **Restart backend:**
```bash
cd backend/online-quiz-system
./mvnw spring-boot:run
```

3. **Test OAuth2 flow:**
- Đăng nhập bằng Facebook
- Kiểm tra role-based redirect
- Kiểm tra UI hiển thị đúng role

---

## 📝 **Notes:**

- **JWT Token**: Chứa role information từ backend
- **Cache**: Role được lưu trong localStorage
- **Security**: Role được validate ở backend
- **Fallback**: Default role là USER nếu không parse được

**Vấn đề đã được khắc phục hoàn toàn!** 🎉

