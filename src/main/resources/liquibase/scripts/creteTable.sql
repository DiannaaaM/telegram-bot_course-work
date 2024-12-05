CREATE TABLE notification_task (
    id BIGINT PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    message_text VARCHAR(255) NOT NULL,
    send_time TIMESTAMP NOT NULL,
    status VARCHAR(50) DEFAULT 'pending' NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);

-- Индексы (если потребуется) можно добавить для chat_id или send_time, например:
CREATE INDEX idx_send_time ON notification_task (send_time);