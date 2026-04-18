CREATE TABLE compliance_decision_rules (
                                           id BIGSERIAL PRIMARY KEY,
                                           decision_table_id BIGINT NOT NULL REFERENCES compliance_decision_tables(id),
                                           rule_code VARCHAR(100) NOT NULL,
                                           policy_code VARCHAR(100) NOT NULL,
                                           policy_version VARCHAR(50) NOT NULL,
                                           scenario_code VARCHAR(100) NOT NULL,
                                           priority INT NOT NULL,
                                           active BOOLEAN NOT NULL DEFAULT TRUE,
                                           min_amount DECIMAL(19,2),
                                           max_amount DECIMAL(19,2),
                                           currency_equals VARCHAR(3),
                                           payment_rail_equals VARCHAR(20),
                                           counterparty_type_equals VARCHAR(30),
                                           source_risk_score_gte INT,
                                           destination_risk_score_gte INT,
                                           recent_transfers_last_hour_gte INT,
                                           source_restricted BOOLEAN,
                                           destination_restricted BOOLEAN,
                                           target_wallet_restricted BOOLEAN,
                                           requires_target_wallet BOOLEAN,
                                           decision VARCHAR(20) NOT NULL,
                                           review_allowed BOOLEAN NOT NULL DEFAULT FALSE,
                                           summary_reason VARCHAR(512) NOT NULL,
                                           legal_context VARCHAR(255) NOT NULL,
                                           internal_policy VARCHAR(255) NOT NULL,
                                           user_facing_explanation VARCHAR(1024) NOT NULL,
                                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_compliance_decision_rules_table_id ON compliance_decision_rules(decision_table_id);
CREATE INDEX idx_compliance_decision_rules_active ON compliance_decision_rules(active);
CREATE INDEX idx_compliance_decision_rules_priority ON compliance_decision_rules(priority);
CREATE INDEX idx_compliance_decision_rules_scenario_code ON compliance_decision_rules(scenario_code);