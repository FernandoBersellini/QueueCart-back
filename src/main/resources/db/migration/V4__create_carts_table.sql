CREATE TABLE carts (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT    NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
