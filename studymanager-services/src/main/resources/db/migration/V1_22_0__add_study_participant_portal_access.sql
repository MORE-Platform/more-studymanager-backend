ALTER TABLE studies
    ADD COLUMN participant_portal_access BOOLEAN DEFAULT FALSE;

CREATE TABLE login_tokens
(
    study_id       INTEGER      NOT NULL,
    participant_id INTEGER      NOT NULL,
    application    VARCHAR(255) NOT NULL,
    code           TEXT         NOT NULL,
    PRIMARY KEY (study_id, participant_id, application),
    CONSTRAINT fk_study FOREIGN KEY (study_id) REFERENCES studies (study_id) ON DELETE CASCADE,
    CONSTRAINT fk_participant FOREIGN KEY (study_id, participant_id) REFERENCES participants (study_id, participant_id) ON DELETE CASCADE
);

CREATE TABLE salt_tokens
(
    study_id       INTEGER NOT NULL,
    participant_id INTEGER NOT NULL,
    salt           TEXT    NOT NULL,
    PRIMARY KEY (study_id, participant_id),
    CONSTRAINT fk_study_salt FOREIGN KEY (study_id) REFERENCES studies (study_id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_salt FOREIGN KEY (study_id, participant_id) REFERENCES participants (study_id, participant_id) ON DELETE CASCADE
);
