CREATE TABLE restricted_party_entries (
                                          id BIGSERIAL PRIMARY KEY,
                                          match_type VARCHAR(30) NOT NULL,
                                          match_value VARCHAR(128) NOT NULL,
                                          entity_name VARCHAR(255) NOT NULL,
                                          source VARCHAR(100) NOT NULL,
                                          source_reference VARCHAR(255),
                                          last_synced_at TIMESTAMP,
                                          active BOOLEAN NOT NULL DEFAULT TRUE,
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_restricted_party_match ON restricted_party_entries(match_type, match_value);