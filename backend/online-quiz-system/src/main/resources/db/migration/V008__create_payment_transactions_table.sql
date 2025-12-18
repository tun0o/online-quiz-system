CREATE TABLE payment_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount BIGINT NOT NULL,
    points_purchased INT NOT NULL,
    vnp_txn_ref VARCHAR(255) UNIQUE NOT NULL,
    status status_enum NOT NULL, -- PENDING, SUCCESS, FAILED
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user FOREIGN KEY (user_id) REFERENCES users(id)
);

COMMENT ON TABLE payment_transactions IS 'Lưu trữ lịch sử các giao dịch thanh toán mua điểm.';
