CREATE TABLE processed_order_events (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT    NOT NULL UNIQUE,
    processed_at TIMESTAMP NOT NULL DEFAULT now()
);
