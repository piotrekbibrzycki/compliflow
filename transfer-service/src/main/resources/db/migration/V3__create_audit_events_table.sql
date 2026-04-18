CREATE TABLE audit_events (
                              id BIGSERIAL PRIMARY KEY,
                              transfer_id BIGINT NOT NULL,
                              source_account VARCHAR(34),
                              target_reference VARCHAR(128),
                              rule_name VARCHAR(100) NOT NULL,
                              decision VARCHAR(20) NOT NULL,
                              reason VARCHAR(512) NOT NULL,
                              legal_context VARCHAR(255) NOT NULL,
                              internal_policy VARCHAR(255) NOT NULL,
                              user_facing_explanation VARCHAR(1024) NOT NULL,
                              metadata_json VARCHAR(2048),
                              reviewed_by VARCHAR(128),
                              integrity_hash VARCHAR(128),
                              on_chain_tx_hash VARCHAR(128),
                              on_chain_verified BOOLEAN DEFAULT FALSE,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_events_transfer_id ON audit_events(transfer_id);
CREATE INDEX idx_audit_events_source_account ON audit_events(source_account);
CREATE INDEX idx_audit_events_decision ON audit_events(decision);