CREATE TABLE products (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    sku         VARCHAR(100)   NOT NULL UNIQUE,
    price       NUMERIC(10, 2) NOT NULL,
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id BIGINT         REFERENCES categories (id),
    created_at  TIMESTAMP      NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP      NOT NULL DEFAULT now()
);
