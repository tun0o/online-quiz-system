-- Thêm cột difficulty_level vào bảng quizzes
ALTER TABLE quiz_submissions ADD COLUMN difficulty_level difficulty_level;

-- Tính toán và cập nhật độ khó cho các quiz hiện có
UPDATE quiz_submissions 
SET difficulty_level = (
    CASE 
        WHEN (
            SELECT AVG(difficulty_level::integer) 
            FROM submission_questions 
            WHERE submission_id = quiz_submissions.id
        ) <= 1.3 THEN 'EASY'::difficulty_level
        WHEN (
            SELECT AVG(difficulty_level::integer) 
            FROM submission_questions 
            WHERE submission_id = quiz_submissions.id
        ) <= 2.3 THEN 'MEDIUM'::difficulty_level
        ELSE 'HARD'::difficulty_level
    END
)
WHERE difficulty_level IS NULL;