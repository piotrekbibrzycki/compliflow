INSERT INTO compliance_decision_tables (table_code, name, description, version, active)
VALUES (
           'TRANSFER_COMPLIANCE_V1',
           'Transfer Compliance Decision Table',
           'Default transfer compliance policy table for payment orchestration, sanctions checks, and wallet screening.',
           '1.0',
           TRUE
       );

INSERT INTO compliance_decision_rules (
    decision_table_id,
    rule_code,
    policy_code,
    policy_version,
    scenario_code,
    priority,
    active,
    source_restricted,
    decision,
    review_allowed,
    summary_reason,
    legal_context,
    internal_policy,
    user_facing_explanation
)
VALUES (
           (SELECT id FROM compliance_decision_tables WHERE table_code = 'TRANSFER_COMPLIANCE_V1'),
           'RULE_RESTRICTED_SOURCE_ACCOUNT',
           'SANCTIONS-ACCOUNT-SOURCE',
           '1.0',
           'RESTRICTED_PARTY_MATCH',
           10,
           TRUE,
           TRUE,
           'BLOCK',
           FALSE,
           'Source account matched restricted-party dataset',
           'Restricted-party / sanctions screening',
           'Block transactions involving restricted source counterparties',
           'This transfer was blocked because the source account matched a restricted-party dataset.'
       );

INSERT INTO compliance_decision_rules (
    decision_table_id,
    rule_code,
    policy_code,
    policy_version,
    scenario_code,
    priority,
    active,
    destination_restricted,
    decision,
    review_allowed,
    summary_reason,
    legal_context,
    internal_policy,
    user_facing_explanation
)
VALUES (
           (SELECT id FROM compliance_decision_tables WHERE table_code = 'TRANSFER_COMPLIANCE_V1'),
           'RULE_RESTRICTED_DESTINATION_ACCOUNT',
           'SANCTIONS-ACCOUNT-DESTINATION',
           '1.0',
           'RESTRICTED_PARTY_MATCH',
           20,
           TRUE,
           TRUE,
           'BLOCK',
           FALSE,
           'Destination account matched restricted-party dataset',
           'Restricted-party / sanctions screening',
           'Block transactions involving restricted destination counterparties',
           'This transfer was blocked because the destination account matched a restricted-party dataset.'
       );

INSERT INTO compliance_decision_rules (
    decision_table_id,
    rule_code,
    policy_code,
    policy_version,
    scenario_code,
    priority,
    active,
    counterparty_type_equals,
    requires_target_wallet,
    target_wallet_restricted,
    decision,
    review_allowed,
    summary_reason,
    legal_context,
    internal_policy,
    user_facing_explanation
)
VALUES (
           (SELECT id FROM compliance_decision_tables WHERE table_code = 'TRANSFER_COMPLIANCE_V1'),
           'RULE_RESTRICTED_TARGET_WALLET',
           'SANCTIONS-WALLET-DESTINATION',
           '1.0',
           'RESTRICTED_WALLET_MATCH',
           30,
           TRUE,
           'WALLET_ADDRESS',
           TRUE,
           TRUE,
           'BLOCK',
           FALSE,
           'Target wallet address matched restricted-party dataset',
           'Restricted-party screening for wallet-based payouts',
           'Block wallet payouts to restricted wallet addresses',
           'This transfer was blocked because the destination wallet address matched a restricted-party dataset.'
       );

INSERT INTO compliance_decision_rules (
    decision_table_id,
    rule_code,
    policy_code,
    policy_version,
    scenario_code,
    priority,
    active,
    min_amount,
    decision,
    review_allowed,
    summary_reason,
    legal_context,
    internal_policy,
    user_facing_explanation
)
VALUES (
           (SELECT id FROM compliance_decision_tables WHERE table_code = 'TRANSFER_COMPLIANCE_V1'),
           'RULE_LARGE_TRANSFER_THRESHOLD',
           'AML-THRESHOLD-LARGE-TRANSFER',
           '1.0',
           'LARGE_TRANSFER_THRESHOLD',
           40,
           TRUE,
           10000.00,
           'FLAG',
           TRUE,
           'Transfer amount exceeds enhanced review threshold',
           'Enhanced due diligence for high-value transfer screening',
           'Flag transfers above 10,000.00',
           'This transfer requires additional review because it exceeds the high-value threshold.'
       );

INSERT INTO compliance_decision_rules (
    decision_table_id,
    rule_code,
    policy_code,
    policy_version,
    scenario_code,
    priority,
    active,
    recent_transfers_last_hour_gte,
    decision,
    review_allowed,
    summary_reason,
    legal_context,
    internal_policy,
    user_facing_explanation
)
VALUES (
           (SELECT id FROM compliance_decision_tables WHERE table_code = 'TRANSFER_COMPLIANCE_V1'),
           'RULE_HIGH_TRANSFER_FREQUENCY',
           'AML-BEHAVIOR-HIGH-FREQUENCY',
           '1.0',
           'HIGH_TRANSFER_FREQUENCY',
           50,
           TRUE,
           6,
           'FLAG',
           TRUE,
           'Unusual transfer frequency detected',
           'Monitoring for unusual transaction patterns',
           'Flag more than 5 transfers from same account within 1 hour',
           'This transfer was flagged because the account has unusually frequent transfer activity.'
       );

INSERT INTO compliance_decision_rules (
    decision_table_id,
    rule_code,
    policy_code,
    policy_version,
    scenario_code,
    priority,
    active,
    source_risk_score_gte,
    decision,
    review_allowed,
    summary_reason,
    legal_context,
    internal_policy,
    user_facing_explanation
)
VALUES (
           (SELECT id FROM compliance_decision_tables WHERE table_code = 'TRANSFER_COMPLIANCE_V1'),
           'RULE_HIGH_RISK_SOURCE_ACCOUNT',
           'AML-RISK-HIGH-SOURCE-ACCOUNT',
           '1.0',
           'HIGH_RISK_ACCOUNT',
           60,
           TRUE,
           71,
           'FLAG',
           TRUE,
           'High-risk account requires manual review',
           'Risk-based enhanced monitoring',
           'Manual review required for accounts above risk score 70',
           'This transfer was flagged because the source account is classified as high risk.'
       );