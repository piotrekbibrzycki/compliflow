ALTER TABLE audit_events
    ADD COLUMN policy_code VARCHAR(100),
    ADD COLUMN policy_version VARCHAR(50),
    ADD COLUMN scenario_code VARCHAR(100);

CREATE INDEX idx_audit_events_policy_code ON audit_events(policy_code);
CREATE INDEX idx_audit_events_scenario_code ON audit_events(scenario_code);