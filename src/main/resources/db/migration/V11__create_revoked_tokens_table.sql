CREATE TABLE revoked_tokens (
    id         BIGSERIAL PRIMARY KEY,
    jti        VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP    NOT NULL,
    revoked_at TIMESTAMP    NOT NULL DEFAULT now()
);
