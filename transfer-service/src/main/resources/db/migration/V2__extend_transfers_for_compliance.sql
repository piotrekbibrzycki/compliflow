ALTER TABLE transfers
    ADD COLUMN target_wallet_address VARCHAR(128),
    ADD COLUMN compliance_decision VARCHAR(20) NOT NULL DEFAULT 'PASS',
    ADD COLUMN compliance_reason_summary VARCHAR(512),
    ADD COLUMN payment_rail VARCHAR(20) NOT NULL DEFAULT 'SEPA',
    ADD COLUMN counterparty_type VARCHAR(30) NOT NULL DEFAULT 'BANK_ACCOUNT',
    ADD COLUMN review_comment VARCHAR(512),
    ADD COLUMN reviewed_by VARCHAR(100),
    ADD COLUMN reviewed_at TIMESTAMP;