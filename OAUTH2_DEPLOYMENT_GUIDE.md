# 🚀 OAuth2 Deployment Guide - Online Quiz System

## 📋 **TỔNG QUAN**

Hệ thống Online Quiz System đã được triển khai hoàn chỉnh OAuth2 integration với Google và Facebook. Hướng dẫn này sẽ giúp bạn deploy hệ thống từ development đến production.

---

## 🔧 **CẤU TRÚC HỆ THỐNG**

### **Backend (Spring Boot)**
- ✅ OAuth2 Google + Facebook integration
- ✅ JWT token generation và management
- ✅ User account linking và creation
- ✅ Security configuration với Spring Security
- ✅ OAuth2 test endpoints

### **Frontend (React)**
- ✅ OAuth2 login buttons (Google + Facebook)
- ✅ OAuth2 success/error handling
- ✅ JWT token management
- ✅ Protected routes

### **Infrastructure**
- ✅ Docker containerization
- ✅ PostgreSQL database
- ✅ Redis cache
- ✅ MinIO object storage

---

## 🛠️ **CÁCH TRIỂN KHAI**

### **1. DEVELOPMENT SETUP**

#### **A. Backend Setup**
```bash
# 1. Copy environment template
cd backend/online-quiz-system
cp env.example .env

# 2. Edit .env with your values
# - JWT_SECRET: Generate with `openssl rand -base64 32`
# - GOOGLE_CLIENT_ID & GOOGLE_CLIENT_SECRET: From Google Cloud Console
# - FACEBOOK_CLIENT_ID & FACEBOOK_CLIENT_SECRET: From Facebook Developers
# - MAIL_USERNAME & MAIL_PASSWORD: Gmail app password

# 3. Run application
./mvnw spring-boot:run
```

#### **B. Frontend Setup**
```bash
# 1. Install dependencies
cd frontend/frontend
npm install

# 2. Create .env file
echo "VITE_API_URL=http://localhost:8080" > .env

# 3. Run development server
npm run dev
```

### **2. PRODUCTION DEPLOYMENT**

#### **A. Environment Variables Setup**
```bash
# 1. Copy production template
cd docker
cp env.production.example .env

# 2. Edit .env with production values
# - Update all URLs to production domains
# - Use strong passwords and secrets
# - Configure OAuth2 redirect URIs for production
```

#### **B. OAuth2 Provider Configuration**

**Google OAuth2 Setup:**
1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create/select project
3. Enable Google+ API
4. Create OAuth2 credentials
5. Add authorized redirect URIs:
   - `https://yourdomain.com/login/oauth2/code/google`
   - `https://api.yourdomain.com/login/oauth2/code/google`

**Facebook OAuth2 Setup:**
1. Go to [Facebook Developers](https://developers.facebook.com/)
2. Create new app
3. Add Facebook Login product
4. Configure OAuth redirect URIs:
   - `https://yourdomain.com/login/oauth2/code/facebook`
   - `https://api.yourdomain.com/login/oauth2/code/facebook`

#### **C. Docker Deployment**
```bash
# 1. Build and start all services
cd docker
docker-compose up -d

# 2. Check service health
docker-compose ps
docker-compose logs backend
docker-compose logs frontend

# 3. Test OAuth2 configuration
curl http://localhost:8080/api/oauth2/test/health
```

---

## 🔒 **SECURITY CHECKLIST**

### **✅ Đã khắc phục:**
- [x] Di chuyển tất cả secrets ra environment variables
- [x] Xóa hardcoded credentials khỏi code
- [x] Sử dụng strong JWT secrets
- [x] Cấu hình CORS properly
- [x] OAuth2 redirect URIs validation

### **⚠️ Cần kiểm tra:**
- [ ] HTTPS configuration trong production
- [ ] Database connection encryption
- [ ] Redis authentication
- [ ] MinIO access policies
- [ ] Rate limiting implementation
- [ ] Security headers configuration

---

## 🧪 **TESTING OAUTH2 INTEGRATION**

### **1. Backend Testing**
```bash
# Test OAuth2 configuration
curl http://localhost:8080/api/oauth2/test/config

# Test OAuth2 URLs
curl http://localhost:8080/api/oauth2/test/urls

# Test health check
curl http://localhost:8080/api/oauth2/test/health
```

### **2. Frontend Testing**
```javascript
// Test OAuth2 URLs
console.log('Google OAuth2 URL:', 'http://localhost:8080/oauth2/authorization/google');
console.log('Facebook OAuth2 URL:', 'http://localhost:8080/oauth2/authorization/facebook');
console.log('Success URL:', 'http://localhost:3000/oauth2/success');
console.log('Error URL:', 'http://localhost:3000/oauth2/error');
```

### **3. Complete OAuth2 Flow Test**
1. Navigate to `http://localhost:3000/login`
2. Click "Đăng nhập với Google" hoặc "Đăng nhập với Facebook"
3. Complete OAuth2 flow
4. Verify redirect to success page
5. Check JWT token in localStorage
6. Verify user data in dashboard

---

## 📊 **MONITORING & LOGGING**

### **OAuth2 Logs**
```bash
# Backend OAuth2 logs
docker-compose logs backend | grep OAuth2

# Frontend OAuth2 logs
docker-compose logs frontend
```

### **Health Checks**
```bash
# Backend health
curl http://localhost:8080/api/oauth2/test/health

# Frontend health
curl http://localhost:3000/
```

---

## 🚀 **PRODUCTION DEPLOYMENT STEPS**

### **1. Pre-deployment Checklist**
- [ ] All environment variables configured
- [ ] OAuth2 providers configured with production URLs
- [ ] SSL certificates ready
- [ ] Database migrations completed
- [ ] Security audit passed

### **2. Deployment Commands**
```bash
# 1. Build production images
docker-compose -f docker-compose.yml build

# 2. Start services
docker-compose -f docker-compose.yml up -d

# 3. Verify deployment
docker-compose ps
curl https://yourdomain.com/api/oauth2/test/health
```

### **3. Post-deployment Verification**
- [ ] OAuth2 login working
- [ ] JWT tokens generated correctly
- [ ] User data stored in database
- [ ] Frontend-backend communication working
- [ ] All health checks passing

---

## 🔧 **TROUBLESHOOTING**

### **Common Issues**

**1. OAuth2 Configuration Errors**
```bash
# Check configuration
curl http://localhost:8080/api/oauth2/test/config

# Check logs
docker-compose logs backend | grep OAuth2
```

**2. CORS Issues**
```bash
# Check CORS configuration
curl -H "Origin: http://localhost:3000" \
     -H "Access-Control-Request-Method: POST" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     http://localhost:8080/api/auth/login
```

**3. JWT Token Issues**
```bash
# Check JWT configuration
curl http://localhost:8080/api/oauth2/test/health
```

---

## 📚 **TÀI LIỆU THAM KHẢO**

- [Spring Security OAuth2 Documentation](https://docs.spring.io/spring-security/reference/servlet/oauth2/index.html)
- [Google OAuth2 Setup Guide](https://developers.google.com/identity/protocols/oauth2)
- [Facebook Login Documentation](https://developers.facebook.com/docs/facebook-login/)
- [Docker Compose Documentation](https://docs.docker.com/compose/)

---

## 🎯 **KẾT LUẬN**

Hệ thống OAuth2 integration đã được triển khai hoàn chỉnh với:

✅ **Backend**: Spring Security OAuth2 + JWT + User management  
✅ **Frontend**: React OAuth2 components + Token management  
✅ **Infrastructure**: Docker + PostgreSQL + Redis + MinIO  
✅ **Security**: Environment variables + CORS + HTTPS ready  
✅ **Testing**: OAuth2 test endpoints + Health checks  
✅ **Production**: Docker deployment + Environment configuration  

Hệ thống sẵn sàng cho production deployment sau khi cấu hình OAuth2 providers và environment variables.

