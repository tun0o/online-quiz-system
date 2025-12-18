-- Enum cho các loại thông báo
CREATE TYPE notification_type_enum AS ENUM (
    'GRADING_COMPLETED',
    'PAYMENT_SUCCESS',
    'PAYMENT_FAILURE',
    'SUBMISSION_APPROVED',
    'SUBMISSION_REJECTED',
    'WELCOME'
);

-- Bảng thông báo
CREATE TABLE notifications (
    id BIGSERIAL PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    link VARCHAR(255),
    notification_type notification_type_enum NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_recipient
        FOREIGN KEY(recipient_id)
        REFERENCES users(id)
        ON DELETE CASCADE
);

-- Indexes để tăng tốc độ truy vấn
CREATE INDEX idx_notifications_recipient_id ON notifications(recipient_id);
CREATE INDEX idx_notifications_is_read ON notifications(is_read);
