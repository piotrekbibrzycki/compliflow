CREATE TABLE compliance_decision_tables (
                                            id BIGSERIAL PRIMARY KEY,
                                            table_code VARCHAR(100) NOT NULL UNIQUE,
                                            name VARCHAR(255) NOT NULL,
                                            description VARCHAR(1000),
                                            version VARCHAR(50) NOT NULL,
                                            active BOOLEAN NOT NULL DEFAULT TRUE,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_compliance_decision_tables_active ON compliance_decision_tables(active);