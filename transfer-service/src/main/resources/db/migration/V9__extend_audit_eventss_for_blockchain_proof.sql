ALTER TABLE audit_events
    ADD COLUMN proof_schema_version VARCHAR(64),
    ADD COLUMN anchor_network VARCHAR(64),
    ADD COLUMN anchored_at TIMESTAMP;

CREATE INDEX idx_audit_events_proof_schema_version ON audit_events(proof_schema_version);
CREATE INDEX idx_audit_events_anchor_network ON audit_events(anchor_network);
CREATE INDEX idx_audit_events_anchored_at ON audit_events(anchored_at);