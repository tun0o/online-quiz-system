# Online Quiz System (Practizz)

Dự án đồ án tốt nghiệp — Hệ thống ôn luyện trắc nghiệm trực tuyến tích hợp AI cá nhân hóa gợi ý bài học.

---

## Tổng quan
Online Quiz System (Practizz) là một nền tảng ôn luyện trắc nghiệm trực tuyến dành cho học sinh/độc giả. Hệ thống hỗ trợ:
- Đăng ký / đăng nhập (bao gồm OAuth2),
- Tạo / quản lý câu hỏi và đề thi (CRUD),
- Làm bài trực tuyến, chấm điểm tự động và yêu cầu chấm bài tự luận,
- Thống kê cá nhân, lịch sử làm bài, thông báo hệ thống,
- Hệ thống đóng góp câu hỏi và quy trình kiểm duyệt cho admin,
- Mua điểm (gói nạp điểm), bảng xếp hạng, nhiệm vụ và các tính năng gamification,
- Hệ thống gợi ý AI ở mức rule-based ban đầu, dự kiến nâng cấp bằng collaborative/content-based recommendation.

---

## Kiến trúc & Cấu trúc thư mục chính
- backend/online-quiz-system — Backend: Spring Boot (Java), Spring Security, JPA, logic ứng dụng.
  - DataInitializer tạo user mặc định (admin@quiz.com / mật khẩu: admin) nếu chưa tồn tại.
- frontend/frontend — Frontend: React + Vite + TailwindCSS.
  - App routes, component admin/user, payment, quiz, tasks, ...
- docker/initdb — Scripts khởi tạo schema PostgreSQL (enum types, bảng, seed cơ bản).
- docker/docker-compose.yml - Script khởi chạy hệ thống qua Docker.
- docs — Tài liệu nội bộ: MVP scope, hướng dẫn reset mật khẩu, v.v.
- README.md — (file này) mô tả tổng quan.

Một số file bạn có thể tham khảo trực tiếp:
- frontend vite config: frontend/frontend/vite.config.js
- frontend entry: frontend/frontend/index.html
- docker init script: docker/initdb/01-init-schema.sql
- backend seed admin: backend/online-quiz-system/src/main/java/.../DataInitializer.java
- docs: docs/MVP_scope.md, docs/PASSWORD_RESET_GUIDE.md

---

## Stack chính
- Backend: Spring Boot 3, Spring Security, Spring Data JPA
- Frontend: React, Vite, TailwindCSS
- DB: PostgreSQL, MinIO(S3)
- Hệ thống thanh toán (ví điểm): module frontend + backend services, tích hợp thanh toán VNPAY
- AI Recommendation: bước đầu là rule-based, hướng tới content-based & collaborative filtering

---

## Tính năng chính
- Role: USER, ADMIN (enum trong DB)
- Xác thực: email/password, OAuth2 (OAuth2Success / OAuth2Error components)
- Quản trị: giao diện admin để duyệt/cấp quyền, chấm bài, quản lý người dùng và đề thi
- Người dùng: làm quiz, xem thống kê, nhiệm vụ, bảng xếp hạng, đóng góp câu hỏi, mua điểm tiêu dùng, yêu cầu chấm câu hỏi tự luận
- Hệ thống kiểm duyệt: đóng góp câu hỏi -> queue kiểm duyệt (status enum PENDING/APPROVED/REJECTED)
- Seed data: default admin được tạo tự động khi khởi động backend (email: admin@quiz.com, mật khẩu: admin)

---

## Cài đặt nhanh (Developer / Local)

Yêu cầu cơ bản:
- Java 17+ (tùy phiên bản Spring Boot)
- Node.js 18+ (hoặc LTS tương thích)
- PostgreSQL (hoặc Docker)
- MinIO (hoặc Docker)
- Docker & docker-compose (nếu muốn chạy BE, FE, DB và MinIO bằng container)

1) Clone project
```bash
git clone https://github.com/tun0o/online-quiz-system.git
cd online-quiz-system
```

2) Cấu hình môi trường
- Backend: tạo file .env hoặc application.properties/application.yml theo mẫu (các biến quan trọng)
  - SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/yourdb
  - SPRING_DATASOURCE_USERNAME=postgres
  - SPRING_DATASOURCE_PASSWORD=postgres
  - SPRING_JPA_HIBERNATE_DDL_AUTO=update (hoặc validate)
  - JWT_SECRET=change_this_to_a_secure_value
  - MAIL_* (SMTP cấu hình nếu dùng tính năng email)
- Frontend: frontend/frontend/.env (ví dụ)
  - VITE_API_BASE_URL=http://localhost:8080/api

3) Khởi chạy hệ thống
- Sử dụng Docker Desktop
```bash
cd docker
docker-compose up --build -d
```

- Public bucket MinIO
```bash
docker exec -it quiz_minio /bin/sh
mc alias set myminio http://minio:9000 minio minio123
mc ls myminio
mc anonymous set download myminio/practizz-bucket
```

---

## Biến môi trường mẫu
Backend (application.yml / .env):
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quizdb
- SPRING_DATASOURCE_USERNAME=postgres
- SPRING_DATASOURCE_PASSWORD=123456
- SPRING_PROFILES_ACTIVE=dev
- JWT_SECRET=your_jwt_secret_here
- OAUTH2_CLIENT_ID=...
- OAUTH2_CLIENT_SECRET=...
- MAIL_HOST=smtp.example.com
- MAIL_PORT=587
- MAIL_USERNAME=...
- MAIL_PASSWORD=...

Frontend (frontend/.env):
- VITE_API_BASE_URL=http://localhost:8080/api
- VITE_OTHER_CONFIG=...

---

## Database & Seed
- Script tạo schema: docker\initdb\schema.sql (bao gồm enum types như role_enum, subject_enum, status_enum và bảng users, questions, quizzes...)
- Backend có DataInitializer (backend/.../DataInitializer.java) dùng để tạo default admin nếu chưa có (email admin@quiz.com).

---