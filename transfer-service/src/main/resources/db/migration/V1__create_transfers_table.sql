CREATE TABLE transfers (
                           id BIGSERIAL PRIMARY KEY,
                           from_account VARCHAR(255) NOT NULL,
                           to_account VARCHAR(255) NOT NULL,
                           amount DECIMAL(19,2) NOT NULL,
                           title VARCHAR(255) NOT NULL,
                           currency VARCHAR(3) NOT NULL,
                           status VARCHAR(255) NOT NULL,
                           failure_reason VARCHAR(255),
                           created_at TIMESTAMP NOT NULL,
                           completed_at TIMESTAMP
);