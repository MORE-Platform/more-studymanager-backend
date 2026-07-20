-- Observation Group Update
--
-- 1. Extension for multiple assignments of ObervationGroups to Observation
-- 2. Extension for multiple assignments of ObervationGroups to Intervention

-- Allow for multiple assignment of observations to observation_groups
DROP INDEX observations_observation_group_id;

ALTER TABLE observations
    DROP COLUMN observation_group_id;

CREATE TABLE observation_observation_groups (
    study_id BIGINT NOT NULL,
    observation_id INT NOT NULL,
    observation_group_id INT NOT NULL,

    PRIMARY KEY (study_id, observation_id, observation_group_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, observation_group_id) REFERENCES observation_groups(study_id, observation_group_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id) ON DELETE CASCADE
);

-- Allow for multiple assignment of interventions to observation_groups
DROP INDEX interventions_observation_group_id;

ALTER TABLE interventions
    DROP COLUMN observation_group_id;

CREATE TABLE intervention_observation_groups (
    study_id BIGINT NOT NULL,
    intervention_id INT NOT NULL,
    observation_group_id INT NOT NULL,

    PRIMARY KEY (study_id, intervention_id, observation_group_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, observation_group_id) REFERENCES observation_groups(study_id, observation_group_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, intervention_id) REFERENCES interventions(study_id, intervention_id) ON DELETE CASCADE
);
