# 🚀 STARTUP GUIDE - Online Quiz System

## ✅ Sửa Lỗi Actuator Health Check

### 🔧 Các Thay Đổi Đã Thực Hiện:

1. **Fixed HealthCheckService**: Loại bỏ dependency trực tiếp vào `HealthIndicator`
2. **Updated HealthController**: Sử dụng custom health check methods
3. **Disabled Caching**: Tạm thời disable Redis caching để tránh lỗi connection
4. **Added Fallback Health Check**: `CustomHealthIndicator` đơn giản hơn

## 🏃‍♂️ Cách Khởi Động

### 1. **Khởi động cơ bản (không Redis)**:
```bash
cd backend/online-quiz-system

# Set environment variables
export JWT_SECRET="your-jwt-secret-key-base64"
export MAIL_USERNAME="your-email@gmail.com"
export MAIL_PASSWORD="your-app-password"
export GOOGLE_CLIENT_ID="your-google-client-id"
export GOOGLE_CLIENT_SECRET="your-google-client-secret"

# Start application
mvn spring-boot:run
```

### 2. **Khởi động với Docker (Full Stack)**:
```bash
# Start database and Redis
cd docker
docker-compose up -d

# Wait for database to be ready
sleep 10

# Start backend
cd ../backend/online-quiz-system
mvn spring-boot:run
```

## 🔍 Kiểm Tra Health Endpoints

Sau khi khởi động thành công, test các endpoints:

```bash
# Basic health check
curl http://localhost:8080/api/health

# Database health
curl http://localhost:8080/api/health/database

# System metrics
curl http://localhost:8080/api/health/metrics

# Readiness check
curl http://localhost:8080/api/health/ready

# Liveness check
curl http://localhost:8080/api/health/live
```

## 🎯 Expected Health Response

```json
{
  "database_status": "CONNECTED",
  "repository_status": "FUNCTIONAL",
  "total_users": 0,
  "total_profiles": 0,
  "profile_ratio": 0,
  "data_integrity_status": "CHECKED",
  "users_without_profiles": 0,
  "orphan_profiles": 0,
  "data_integrity_healthy": true,
  "system_metrics": {
    "total_memory_mb": 512,
    "free_memory_mb": 256,
    "used_memory_mb": 256,
    "max_memory_mb": 1024,
    "available_processors": 8
  },
  "timestamp": "2024-10-02T...",
  "overall_status": "HEALTHY",
  "status_code": "UP"
}
```

## ⚡ Kích Hoạt Redis Caching (Optional)

Khi muốn enable Redis caching:

1. **Đảm bảo Redis đang chạy**:
```bash
docker run -d -p 6379:6379 redis:7
```

2. **Uncomment cache configurations**:
```java
// In CacheConfig.java
@EnableCaching  // Remove comment
@Bean  // Remove comment
```

3. **Uncomment cache annotations**:
```java
// In UserRepository.java và UserProfileRepository.java
@Cacheable(value = "users", key = "#email")  // Remove comment
```

## 🛠️ Troubleshooting

### Lỗi Database Connection:
```bash
# Check PostgreSQL
docker ps | grep postgres
docker logs quiz_postgres
```

### Lỗi Redis Connection:
```bash
# Check Redis
docker ps | grep redis
docker logs quiz_redis
```

### Lỗi JWT Secret:
```bash
# Generate a new secret
openssl rand -base64 32
```

## 📊 Development vs Production

### Development Mode:
- JPA show-sql: `true`
- Log level: `DEBUG`
- Cache: Disabled
- Redis: Optional

### Production Mode:
- JPA show-sql: `false`
- Log level: `INFO`
- Cache: Enabled
- Redis: Required

## ✅ Success Indicators

Backend khởi động thành công khi:
- ✅ Application starts on port 8080
- ✅ Database connection established
- ✅ Health endpoints return 200 OK
- ✅ No error logs in console
- ✅ Flyway migrations run successfully

Backend sẵn sàng khi thấy log:
```
Started OnlineQuizSystemApplication in X.XXX seconds
```

