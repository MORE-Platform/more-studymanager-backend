CREATE TABLE participant_applications
(
    study_id       INTEGER      NOT NULL,
    participant_id INTEGER      NOT NULL,
    application    VARCHAR(255) NOT NULL,
    uuid           UUID         NOT NULL,
    PRIMARY KEY (study_id, participant_id, application),
    CONSTRAINT fk_study_pa FOREIGN KEY (study_id) REFERENCES studies (study_id) ON DELETE CASCADE,
    CONSTRAINT fk_participant_pa FOREIGN KEY (study_id, participant_id) REFERENCES participants (study_id, participant_id) ON DELETE CASCADE
);
