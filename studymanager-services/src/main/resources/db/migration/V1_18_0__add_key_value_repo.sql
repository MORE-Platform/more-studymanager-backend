CREATE TABLE participant_key_value
(
    study_id       BIGINT       NOT NULL,
    participant_id INTEGER      NOT NULL,
    key            VARCHAR(255) NOT NULL,
    value          JSONB        NOT NULL,
    created        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    modified       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (study_id, participant_id, key),
    CONSTRAINT fk_participant_kv_participant
        FOREIGN KEY (study_id, participant_id)
            REFERENCES participants (study_id, participant_id)
            ON DELETE CASCADE
);

CREATE INDEX idx_participant_kv_key ON participant_key_value (key);
CREATE INDEX idx_participant_kv_value ON participant_key_value USING gin (value);