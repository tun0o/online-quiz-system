# Docker Environment Setup

## 📝 Environment Variables for Docker

Create a `.env` file in the `docker/` directory:

```bash
# Database Configuration
DB_NAME=quizdb
DB_USERNAME=quizuser
DB_PASSWORD=quizpass

# MinIO Configuration
MINIO_ROOT_USER=minio
MINIO_ROOT_PASSWORD=minio123
```

## 🚀 Usage

```bash
cd docker/
# Create .env file with above content
echo "DB_NAME=quizdb" > .env
echo "DB_USERNAME=quizuser" >> .env
echo "DB_PASSWORD=quizpass" >> .env

# Start services
docker-compose up -d
```
