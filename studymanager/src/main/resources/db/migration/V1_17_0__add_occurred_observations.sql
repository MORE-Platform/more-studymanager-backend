-- create table for storing audit_logs
CREATE TYPE observation_data_state AS ENUM ('missing', 'incomplete', 'partial', 'complete');

CREATE TABLE occurred_observation (
    study_id BIGINT NOT NULL,
    observation_id INT NOT NULL,
    participant_id INT NOT NULL,
    start TIMESTAMPTZ NOT NULL,
    "end" TIMESTAMPTZ NOT NULL,
    data_valid BOOLEAN NOT NULL DEFAULT TRUE,
    data_state observation_data_state NOT NULL DEFAULT 'missing'::observation_data_state,
    properties JSONB,
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, observation_id, participant_id, start),
    FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id) ON DELETE CASCADE
);

CREATE INDEX occurred_observation_by_participant ON occurred_observation (study_id, participant_id);
CREATE INDEX occurred_observation_by_observation ON occurred_observation (study_id, observation_id);
CREATE INDEX occurred_observation_by_participant_and_observation ON occurred_observation (study_id, participant_id, observation_id);
CREATE INDEX occurred_observation_not_complete ON occurred_observation (study_id)
    WHERE data_state != 'complete'::observation_data_state;
