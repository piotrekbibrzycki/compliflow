INSERT INTO users (email, password_hash, role, created_at)
VALUES (
           'admin@compliflow.local',
           '$2a$10$aXmue19O/wonaKtNEMdaL.LcSalmdH9cgJdr18MIrQtTVfAfAx82m',
           'ADMIN',
           CURRENT_TIMESTAMP
       )
ON CONFLICT (email) DO NOTHING;
