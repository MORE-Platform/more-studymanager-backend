CREATE TABLE participant_observation_properties (
      study_id BIGINT NOT NULL,
      participant_id INT NOT NULL,
      observation_id INT NOT NULL,
      properties JSONB NOT NULL,

      PRIMARY KEY (study_id, participant_id, observation_id),
      FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id) ON DELETE CASCADE,
      FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id) ON DELETE CASCADE
);
