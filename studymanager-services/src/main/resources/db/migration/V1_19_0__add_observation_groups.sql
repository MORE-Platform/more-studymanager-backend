-- Observation Groups
--
-- An observation_group allows to group observations and interventions the belog together.
-- Study participants can be assigned to [0..n] observation groups.
-- The intended use cases for observation_groups are:
-- 1. Define an observation group for a specific indication. Define related observations and intervention
-- within this observation group. Assign participants based on their indications to the relevant observation groups
-- 2. Define an observation group to group observations and interventions that depend on a physical device.
-- Assign all participants with this device to that observation group.

CREATE TABLE observation_groups (
    study_id BIGINT NOT NULL,
    observation_group_id INT NOT NULL,
    title VARCHAR,
    purpose TEXT,
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, observation_group_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE
);

CREATE INDEX observation_groups_study_id ON observation_groups(study_id);

-- Study observations can be assigned [0..1] to an observation_groups
ALTER TABLE observations
    ADD COLUMN observation_group_id INT,

    ADD FOREIGN KEY (study_id, observation_group_id) REFERENCES observation_groups(study_id, observation_group_id) ON DELETE SET NULL (observation_group_id);

CREATE INDEX observations_observation_group_id ON observations(study_id, observation_group_id);

-- Study interventions can be assigned [0..1] to an observation_groups
ALTER TABLE interventions
    ADD COLUMN observation_group_id INT,

    ADD FOREIGN KEY (study_id, observation_group_id) REFERENCES observation_groups(study_id, observation_group_id) ON DELETE SET NULL (observation_group_id);

CREATE INDEX interventions_observation_group_id ON interventions(study_id, observation_group_id);

-- Study participants can be in [0..n] observation_groups
CREATE TABLE participant_observation_groups (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    observation_group_id INT NOT NULL,

    PRIMARY KEY (study_id, participant_id, observation_group_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, observation_group_id) REFERENCES observation_groups(study_id, observation_group_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id) ON DELETE CASCADE
);
