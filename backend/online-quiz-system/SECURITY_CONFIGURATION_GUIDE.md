# Security Configuration Guide

## Tổng quan
Hướng dẫn này mô tả cách cấu hình bảo mật cho Online Quiz System, bao gồm quản lý secrets, environment variables và các biện pháp bảo mật.

## Vấn đề bảo mật đã được khắc phục

### ❌ Vấn đề trước đây:
- **Secrets hardcoded** trong repository
- **Thiếu quản lý environment variables**
- **Không có bảo vệ secrets** trong version control
- **Cấu hình không an toàn** cho production

### ✅ Giải pháp đã triển khai:
- **Environment variables** thay thế hardcoded secrets
- **Scripts quản lý secrets** tự động
- **Gitignore bảo vệ** sensitive files
- **Validation scripts** kiểm tra bảo mật

## Cấu trúc bảo mật

### 1. Environment Variables Management

#### File `.env` (KHÔNG commit vào git)
```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/quizdb
DB_USERNAME=quizuser
DB_PASSWORD=your-secure-database-password

# JWT
JWT_SECRET=your-jwt-secret-key-base64-encoded-minimum-256-bits

# OAuth2
GOOGLE_CLIENT_ID=your-google-client-id.apps.googleusercontent.com
GOOGLE_CLIENT_SECRET=your-google-client-secret
FACEBOOK_CLIENT_ID=your-facebook-client-id
FACEBOOK_CLIENT_SECRET=your-facebook-client-secret

# Mail
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

#### File `env.secure.template` (Template an toàn)
- Chứa template cho tất cả environment variables
- KHÔNG chứa secrets thực tế
- Có thể commit vào git

### 2. Application Properties Security

#### Trước đây (KHÔNG AN TOÀN):
```properties
# ❌ Hardcoded secrets
spring.security.oauth2.client.registration.google.client-id=876217209626-thtrq8duhqm4klgmmd8rrvetp33f6qml.apps.googleusercontent.com
spring.security.oauth2.client.registration.google.client-secret=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
app.jwt.secret=mySecretKey123456789012345678901234567890123456789012345678901234567890
```

#### Bây giờ (AN TOÀN):
```properties
# ✅ Environment variables
spring.security.oauth2.client.registration.google.client-id=${GOOGLE_CLIENT_ID}
spring.security.oauth2.client.registration.google.client-secret=${GOOGLE_CLIENT_SECRET}
app.jwt.secret=${JWT_SECRET}
```

### 3. Scripts Quản lý Secrets

#### `scripts/setup-secrets.sh`
- Tạo file `.env` tự động
- Generate secrets mạnh
- Set permissions bảo mật
- Validation environment

#### `scripts/validate-secrets.sh`
- Kiểm tra tất cả environment variables
- Validate strength của secrets
- Check file permissions
- Detect hardcoded secrets

### 4. Gitignore Protection

#### Patterns được bảo vệ:
```gitignore
# Environment files
.env
.env.local
.env.development
.env.production
*.env

# Secret files
*secret*
*password*
*key*
*credential*
*token*
*api-key*
*client-secret*

# Configuration files
application-secret.properties
application-prod.properties
```

## Hướng dẫn sử dụng

### 1. Setup lần đầu

#### Bước 1: Chạy setup script
```bash
# Tạo file .env với secrets mạnh
./scripts/setup-secrets.sh
```

#### Bước 2: Cập nhật secrets thực tế
```bash
# Edit file .env
nano .env

# Cập nhật các giá trị thực tế:
# - GOOGLE_CLIENT_ID: Từ Google Cloud Console
# - GOOGLE_CLIENT_SECRET: Từ Google Cloud Console
# - FACEBOOK_CLIENT_ID: Từ Facebook Developers
# - FACEBOOK_CLIENT_SECRET: Từ Facebook Developers
# - MAIL_USERNAME: Email của bạn
# - MAIL_PASSWORD: App password của email
```

#### Bước 3: Validate configuration
```bash
# Kiểm tra tất cả secrets
./scripts/validate-secrets.sh
```

### 2. Development Workflow

#### Mỗi lần pull code:
```bash
# 1. Pull latest code
git pull origin main

# 2. Kiểm tra .env file
ls -la .env

# 3. Nếu không có .env, tạo mới
./scripts/setup-secrets.sh
```

#### Trước khi commit:
```bash
# 1. Validate secrets
./scripts/validate-secrets.sh

# 2. Kiểm tra git status
git status

# 3. Đảm bảo không có secrets trong staging
git diff --cached
```

### 3. Production Deployment

#### Environment Variables trong Production:
```bash
# Set environment variables
export JWT_SECRET="your-production-jwt-secret"
export GOOGLE_CLIENT_ID="your-production-google-client-id"
export GOOGLE_CLIENT_SECRET="your-production-google-client-secret"
export FACEBOOK_CLIENT_ID="your-production-facebook-client-id"
export FACEBOOK_CLIENT_SECRET="your-production-facebook-client-secret"
export DB_PASSWORD="your-production-db-password"
```

#### Docker Environment:
```dockerfile
# Dockerfile
ENV JWT_SECRET=""
ENV GOOGLE_CLIENT_ID=""
ENV GOOGLE_CLIENT_SECRET=""
# ... other environment variables
```

#### Docker Compose:
```yaml
# docker-compose.yml
environment:
  - JWT_SECRET=${JWT_SECRET}
  - GOOGLE_CLIENT_ID=${GOOGLE_CLIENT_ID}
  - GOOGLE_CLIENT_SECRET=${GOOGLE_CLIENT_SECRET}
```

## Best Practices

### 1. Secrets Management

#### ✅ Nên làm:
- Sử dụng environment variables
- Generate secrets mạnh (32+ characters)
- Rotate secrets định kỳ
- Sử dụng different secrets cho mỗi environment
- Encrypt sensitive data trong database

#### ❌ Không nên làm:
- Hardcode secrets trong code
- Commit .env file vào git
- Sử dụng weak passwords
- Share secrets qua email/chat
- Store secrets trong logs

### 2. Environment Separation

#### Development:
```bash
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://localhost:5432/quizdb_dev
```

#### Staging:
```bash
SPRING_PROFILES_ACTIVE=staging
DB_URL=jdbc:postgresql://staging-db:5432/quizdb_staging
```

#### Production:
```bash
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:postgresql://prod-db:5432/quizdb_prod
```

### 3. Security Monitoring

#### Logs Security:
```properties
# Không log sensitive data
logging.level.org.springframework.security.oauth2=INFO
logging.level.com.example.online_quiz_system=INFO

# Không log SQL với parameters
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
```

#### Health Checks:
```properties
# Enable security health checks
management.endpoint.health.show-details=when-authorized
management.endpoint.health.show-components=always
```

## Troubleshooting

### 1. Common Issues

#### Issue: "Environment variable not found"
```bash
# Solution: Check .env file exists
ls -la .env

# Solution: Source .env file
source .env

# Solution: Check variable name
echo $JWT_SECRET
```

#### Issue: "OAuth2 configuration invalid"
```bash
# Solution: Validate OAuth2 credentials
./scripts/validate-secrets.sh

# Solution: Check Google/Facebook console
# - Verify redirect URIs
# - Check client ID/secret
# - Ensure scopes are correct
```

#### Issue: "Database connection failed"
```bash
# Solution: Check database credentials
echo $DB_PASSWORD

# Solution: Test database connection
psql -h localhost -U quizuser -d quizdb
```

### 2. Security Validation

#### Check for hardcoded secrets:
```bash
# Search for potential hardcoded secrets
grep -r "GOCSPX-" src/
grep -r "876217209626" src/
grep -r "1562613938059764" src/
```

#### Validate file permissions:
```bash
# Check .env permissions
ls -la .env
# Should be: -rw------- (600)

# Fix permissions if needed
chmod 600 .env
```

## Security Checklist

### ✅ Pre-commit Checklist:
- [ ] Không có hardcoded secrets trong code
- [ ] .env file không được commit
- [ ] File permissions đúng (600 cho .env)
- [ ] Environment variables được validate
- [ ] OAuth2 credentials đúng
- [ ] Database credentials an toàn

### ✅ Pre-deployment Checklist:
- [ ] Production secrets khác development
- [ ] Environment variables được set đúng
- [ ] Database connection test thành công
- [ ] OAuth2 flow test thành công
- [ ] Security headers được set
- [ ] HTTPS được enable

### ✅ Post-deployment Checklist:
- [ ] Application start thành công
- [ ] Health checks pass
- [ ] OAuth2 login hoạt động
- [ ] Database connection stable
- [ ] Logs không chứa sensitive data
- [ ] Security monitoring active

## Emergency Procedures

### 1. Secrets Compromise

#### Nếu secrets bị lộ:
```bash
# 1. Ngay lập tức rotate secrets
./scripts/setup-secrets.sh

# 2. Update OAuth2 credentials
# - Google Cloud Console
# - Facebook Developers

# 3. Update database password
# - Change DB password
# - Update .env file

# 4. Restart application
# - Stop application
# - Update environment
# - Start application
```

#### Nếu .env file bị commit:
```bash
# 1. Remove from git history
git filter-branch --force --index-filter \
  'git rm --cached --ignore-unmatch .env' \
  --prune-empty --tag-name-filter cat -- --all

# 2. Force push
git push origin --force --all

# 3. Regenerate secrets
./scripts/setup-secrets.sh
```

## Kết luận

Hệ thống bảo mật đã được triển khai hoàn chỉnh với:
- ✅ Environment variables thay thế hardcoded secrets
- ✅ Scripts tự động quản lý secrets
- ✅ Gitignore bảo vệ sensitive files
- ✅ Validation scripts kiểm tra bảo mật
- ✅ Documentation đầy đủ

**Lưu ý quan trọng**: Luôn luôn giữ bí mật các environment variables và không bao giờ commit file `.env` vào version control!

