CREATE TABLE notifications (
    id                   BIGSERIAL PRIMARY KEY,
    order_id             BIGINT       NOT NULL,
    recipient_email      VARCHAR(255) NOT NULL,
    notification_status  VARCHAR(50)  NOT NULL,
    created_at           TIMESTAMP    NOT NULL DEFAULT now(),
    sent_at              TIMESTAMP
);

