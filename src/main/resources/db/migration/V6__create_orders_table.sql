CREATE TABLE orders (
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT       NOT NULL,
    order_status VARCHAR(50)  NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT now()
);
