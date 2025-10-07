# Online Quiz System - Docker Setup

## 🚀 Quick Start

### 1. Dừng và xóa containers cũ (nếu có)
```bash
docker-compose down -v
docker volume prune -f
```

### 2. Chạy Docker containers
```bash
docker-compose up -d
```

### 3. Kiểm tra trạng thái
```bash
docker-compose ps
docker-compose logs db
```

## 📊 Services

### Database (PostgreSQL)
- **Port**: 5432
- **Database**: quizdb
- **Username**: quizuser
- **Password**: quizpass
- **Schema**: Tự động tạo với OAuth2Account support

### Redis Cache
- **Port**: 6379
- **Purpose**: Session storage và caching

### MinIO Object Storage
- **API Port**: 9000
- **Console Port**: 9001
- **Username**: minio
- **Password**: minio123
- **Purpose**: File storage (avatars, documents)

## 🔧 Configuration

### Environment Variables
Tạo file `.env` trong thư mục `docker/` với nội dung:

```env
# Database
DB_NAME=quizdb
DB_USERNAME=quizuser
DB_PASSWORD=quizpass

# MinIO
MINIO_ROOT_USER=minio
MINIO_ROOT_PASSWORD=minio123

# Application
SPRING_PROFILES_ACTIVE=docker
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/quizdb
SPRING_DATASOURCE_USERNAME=quizuser
SPRING_DATASOURCE_PASSWORD=quizpass
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

## 🗄️ Database Schema

### Core Tables
- `users` - Core user data
- `oauth2_accounts` - OAuth2 authentication (multiple providers per user)
- `user_profiles` - Extended profile information
- `verification_tokens` - Email verification
- `password_reset_tokens` - Password recovery

### Quiz System Tables
- `quiz_submissions` - Quiz submissions from contributors
- `submission_questions` - Questions within submissions
- `submission_answer_options` - Answer options for questions

### Features
- ✅ **OAuth2Account support** - Multiple OAuth2 providers per user
- ✅ **Comprehensive indexing** - Optimized for performance
- ✅ **Foreign key constraints** - Data integrity
- ✅ **Updated_at triggers** - Automatic timestamp updates
- ✅ **Sample data** - Test data included

## 🔍 Verification

### Check Database Connection
```bash
docker exec -it quiz_postgres psql -U quizuser -d quizdb -c "\dt"
```

### Check Tables
```bash
docker exec -it quiz_postgres psql -U quizuser -d quizdb -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"
```

### Check Sample Data
```bash
docker exec -it quiz_postgres psql -U quizuser -d quizdb -c "SELECT * FROM users;"
docker exec -it quiz_postgres psql -U quizuser -d quizdb -c "SELECT * FROM oauth2_accounts;"
```

## 🛠️ Troubleshooting

### Reset Everything
```bash
docker-compose down -v
docker system prune -f
docker-compose up -d
```

### Check Logs
```bash
docker-compose logs db
docker-compose logs redis
docker-compose logs minio
```

### Database Issues
```bash
# Connect to database
docker exec -it quiz_postgres psql -U quizuser -d quizdb

# Check schema
\dt
\d users
\d oauth2_accounts
```

## 📝 Notes

- Database schema được tạo tự động khi container khởi động
- Sample data được insert để test
- Tất cả tables đều có proper indexing
- OAuth2Account table hỗ trợ multiple providers per user
- Foreign key constraints đảm bảo data integrity
