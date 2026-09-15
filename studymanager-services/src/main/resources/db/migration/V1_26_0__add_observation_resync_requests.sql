-- Observation resync requests
--
-- An operator-triggered request to re-collect the data of one observation for one participant,
-- used when the occurred_observation state is 'missing' or 'incomplete'. The row IS the request:
-- the storage gateway picks it up and deletes it once the data has been synced.
-- Only observations whose factory reports resyncable=true can be requested (enforced in the API layer).

CREATE TABLE observation_resync_requests (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    observation_id INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, participant_id, observation_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id) ON DELETE CASCADE
);

CREATE INDEX observation_resync_requests_study_id ON observation_resync_requests(study_id);
CREATE INDEX observation_resync_requests_study_participant ON observation_resync_requests(study_id, participant_id);
