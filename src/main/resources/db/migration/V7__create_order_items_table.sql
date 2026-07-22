CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT         NOT NULL REFERENCES orders (id) ON DELETE CASCADE,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(255)   NOT NULL,
    unit_price   NUMERIC(10, 2) NOT NULL,
    quantity     INTEGER        NOT NULL
);
