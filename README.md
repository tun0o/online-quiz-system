# Online Quiz System (Practizz)

Dự án đồ án tốt nghiệp — Hệ thống ôn luyện trắc nghiệm trực tuyến tích hợp AI cá nhân hóa gợi ý bài học.

---

## Tổng quan
Online Quiz System (Practizz) là một nền tảng ôn luyện trắc nghiệm trực tuyến dành cho học sinh/độc giả. Hệ thống hỗ trợ:
- Đăng ký / đăng nhập (bao gồm OAuth2),
- Tạo / quản lý câu hỏi và đề thi (CRUD),
- Làm bài trực tuyến, chấm điểm tự động và quản lý bài thi nộp tay,
- Thống kê cá nhân (bảng điểm, tiến độ),
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
- DB: PostgreSQL
- Hệ thống thanh toán (ví điểm): module frontend + backend services, tích hợp thanh toán VNPAY
- AI Recommendation: bước đầu là rule-based, hướng tới content-based & collaborative filtering

---

## Tính năng chính
- Role: USER, ADMIN (enum trong DB)
- Xác thực: email/password, OAuth2 (OAuth2Success / OAuth2Error components)
- Quản trị: giao diện admin để duyệt/cấp quyền, chấm bài, quản lý người dùng và đề thi
- Người dùng: làm quiz, xem thống kê, nhiệm vụ, đóng góp câu hỏi, mua điểm tiêu dùng
- Hệ thống kiểm duyệt: đóng góp câu hỏi -> queue kiểm duyệt (status enum PENDING/APPROVED/REJECTED)
- Seed data: default admin được tạo tự động khi khởi động backend (email: admin@quiz.com, mật khẩu: admin)

---

## Cài đặt nhanh (Developer / Local)

Yêu cầu cơ bản:
- Java 17+ (tùy phiên bản Spring Boot)
- Node.js 18+ (hoặc LTS tương thích)
- PostgreSQL (hoặc Docker)
- Docker & docker-compose (nếu muốn chạy DB bằng container)
- Redis, MinIO nếu bạn muốn chạy toàn bộ dịch vụ có tính năng tương ứng

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
  - SPRING_JPA_HIBERNATE_DDL_AUTO=update (hoặc none)
  - JWT_SECRET=change_this_to_a_secure_value
  - MAIL_* (SMTP cấu hình nếu dùng tính năng email)
- Frontend: frontend/frontend/.env (ví dụ)
  - VITE_API_BASE_URL=http://localhost:8080/api

3) Khởi chạy cơ sở dữ liệu (tùy chọn):
- Chạy PostgreSQL thủ công và chạy script khởi tạo:
  - Sử dụng backend/online-quiz-system/src/main/resources/db/migration/schema.sql để tạo types và tables.

4) Chạy backend
- Nếu dùng Maven:
```bash
cd backend/online-quiz-system
# Nếu có mvnw
./mvnw spring-boot:run
# hoặc
mvn spring-boot:run
```
- Hoặc build jar:
```bash
mvn clean package
java -jar target/*.jar
```
Sau khi backend chạy, một user admin mặc định sẽ được tạo (admin@quiz.com / admin) nếu chưa tồn tại.

5) Chạy frontend
```bash
cd frontend/frontend
npm install
npm run dev
# hoặc
pnpm install
pnpm dev
```
Frontend Vite mặc định proxy /api tới http://localhost:8080 (xem vite.config.js).

---

## Biến môi trường mẫu
Backend (application.yml / .env):
- SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/quizdb
- SPRING_DATASOURCE_USERNAME=postgres
- SPRING_DATASOURCE_PASSWORD=postgres
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
- Script tạo schema: backend/online-quiz-system/src/main/resources/db/migration/schema.sql (bao gồm enum types như role_enum, subject_enum, status_enum và bảng users, questions, quizzes...)
- Backend có DataInitializer (backend/.../DataInitializer.java) dùng để tạo default admin nếu chưa có (email admin@quiz.com).

---