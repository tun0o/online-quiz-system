-- File: V007__add_grading_fields_to_user_answers.sql

-- Thêm các cột cần thiết để chấm điểm trực tiếp trên bảng user_answers
ALTER TABLE user_answers ADD COLUMN score DECIMAL(5, 2);
ALTER TABLE user_answers ADD COLUMN admin_feedback TEXT;

-- Comment giải thích:
-- Cột 'score' sẽ lưu điểm mà admin chấm cho câu hỏi tự luận.
-- Nó sẽ là NULL cho các câu hỏi trắc nghiệm hoặc câu tự luận chưa được chấm.
-- Cột 'is_correct' vẫn được sử dụng cho câu trắc nghiệm (TRUE/FALSE) và có thể được đặt là TRUE
-- cho câu tự luận sau khi đã chấm để đánh dấu là đã xử lý.
-- Cột 'admin_feedback' lưu phản hồi của admin cho câu trả lời của người dùng.

-- Chúng ta có thể xem xét loại bỏ bảng `user_essay_answers` trong tương lai
-- để tránh trùng lặp dữ liệu và đơn giản hóa kiến trúc.
