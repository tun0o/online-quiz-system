# =============================================
# SCRIPT THIET LAP BIEN MOI TRUONG CHO SPRING BOOT
# =============================================
# Script nay se set cac bien moi truong can thiet cho Spring Boot

Write-Host "Dang thiet lap bien moi truong cho Spring Boot..." -ForegroundColor Green

# =============================================
# SPRING PROFILE
# =============================================
$env:SPRING_PROFILES_ACTIVE = "dev"
Write-Host "SPRING_PROFILES_ACTIVE = $env:SPRING_PROFILES_ACTIVE" -ForegroundColor Yellow

# =============================================
# DATABASE CONFIGURATION
# =============================================
$env:DB_URL = "jdbc:postgresql://localhost:5432/quizdb"
$env:DB_USERNAME = "quizuser"
$env:DB_PASSWORD = "quizpass"
Write-Host "Database configuration set" -ForegroundColor Yellow

# =============================================
# MAIL CONFIGURATION
# =============================================
$env:MAIL_HOST = "smtp.gmail.com"
$env:MAIL_PORT = "587"
$env:MAIL_USERNAME = "your-email@gmail.com"
$env:MAIL_PASSWORD = "your-app-password"
$env:MAIL_DEBUG = "false"
Write-Host "Mail configuration set" -ForegroundColor Yellow

# =============================================
# JWT CONFIGURATION
# =============================================
$env:JWT_SECRET = "mySecretKey123456789012345678901234567890123456789012345678901234567890"
$env:JWT_EXPIRATION = "86400000"
$env:JWT_REFRESH_EXPIRATION = "604800000"
Write-Host "JWT configuration set" -ForegroundColor Yellow

# =============================================
# OAUTH2 CONFIGURATION - QUAN TRONG!
# =============================================
# BAN PHAI THAY THE CAC GIA TRI NAY BANG THONG TIN THUC
$env:GOOGLE_CLIENT_ID = "1060499449383-qg2mh00k4j55ssher8pgnjaiffn1ea3s.apps.googleusercontent.com"
$env:GOOGLE_CLIENT_SECRET = "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"
$env:FACEBOOK_CLIENT_ID = "your-facebook-client-id"
$env:FACEBOOK_CLIENT_SECRET = "your-facebook-client-secret"

Write-Host "OAUTH2 CONFIGURATION SET - GOOGLE CLIENT ID DA DUOC CAP NHAT!" -ForegroundColor Green
Write-Host "   GOOGLE_CLIENT_ID = $env:GOOGLE_CLIENT_ID" -ForegroundColor Green
Write-Host "   GOOGLE_CLIENT_SECRET = $env:GOOGLE_CLIENT_SECRET" -ForegroundColor Green
Write-Host "   FACEBOOK_CLIENT_ID = $env:FACEBOOK_CLIENT_ID" -ForegroundColor Red
Write-Host "   FACEBOOK_CLIENT_SECRET = $env:FACEBOOK_CLIENT_SECRET" -ForegroundColor Red

# =============================================
# FRONTEND CONFIGURATION
# =============================================
$env:FRONTEND_URL = "http://localhost:3000"
$env:FRONTEND_ORIGIN = "http://localhost:3000"
$env:CORS_ORIGINS = "http://localhost:3000"
Write-Host "Frontend configuration set" -ForegroundColor Yellow

# =============================================
# SERVER CONFIGURATION
# =============================================
$env:SERVER_PORT = "8080"
Write-Host "Server configuration set" -ForegroundColor Yellow

# =============================================
# LOGGING CONFIGURATION
# =============================================
$env:LOG_LEVEL = "INFO"
$env:OAUTH2_LOG_LEVEL = "DEBUG"
$env:REST_LOG_LEVEL = "INFO"
$env:JPA_SHOW_SQL = "true"
$env:JPA_FORMAT_SQL = "true"
Write-Host "Logging configuration set" -ForegroundColor Yellow

# =============================================
# PERFORMANCE CONFIGURATION
# =============================================
$env:DB_POOL_SIZE = "20"
$env:DB_POOL_MIN = "5"
$env:DB_IDLE_TIMEOUT = "300000"
$env:DB_MAX_LIFETIME = "1800000"
$env:DB_CONNECTION_TIMEOUT = "20000"
$env:DB_LEAK_DETECTION = "60000"
$env:JPA_BATCH_SIZE = "20"
$env:JPA_STATS = "false"
Write-Host "Performance configuration set" -ForegroundColor Yellow

# =============================================
# REDIS CONFIGURATION
# =============================================
$env:REDIS_HOST = "localhost"
$env:REDIS_PORT = "6379"
$env:REDIS_PASSWORD = ""
$env:REDIS_TIMEOUT = "2000ms"
$env:REDIS_POOL_MAX_ACTIVE = "8"
$env:REDIS_POOL_MAX_IDLE = "8"
$env:REDIS_POOL_MIN_IDLE = "0"
$env:CACHE_TTL = "600000"
Write-Host "Redis configuration set" -ForegroundColor Yellow

# =============================================
# KIEM TRA CAU HINH
# =============================================
Write-Host "`nKIEM TRA CAU HINH:" -ForegroundColor Cyan
Write-Host "GOOGLE_CLIENT_ID = $env:GOOGLE_CLIENT_ID" -ForegroundColor White
Write-Host "GOOGLE_CLIENT_SECRET = $env:GOOGLE_CLIENT_SECRET" -ForegroundColor White
Write-Host "FACEBOOK_CLIENT_ID = $env:FACEBOOK_CLIENT_ID" -ForegroundColor White
Write-Host "FACEBOOK_CLIENT_SECRET = $env:FACEBOOK_CLIENT_SECRET" -ForegroundColor White

Write-Host "`nHOAN THANH! Bien moi truong da duoc thiet lap." -ForegroundColor Green
Write-Host "GOOGLE OAUTH2 DA SAN SANG! Ban co the dang nhap bang Google account." -ForegroundColor Green
Write-Host "LUU Y: Ban van can thay the cac gia tri Facebook OAuth2 neu muon su dung Facebook login." -ForegroundColor Yellow
Write-Host "Xem huong dan chi tiet trong file ENV_SETUP_GUIDE.md" -ForegroundColor Blue