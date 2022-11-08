CREATE TYPE study_state AS ENUM ('draft', 'active', 'paused', 'closed');

CREATE TABLE studies (
    study_id BIGSERIAL PRIMARY KEY,
    title VARCHAR,
    purpose TEXT,
    participant_info TEXT,
    consent_info TEXT,
    status study_state  NOT NULL DEFAULT 'draft',
    start_date DATE,
    end_date DATE,
    planned_start_date DATE,
    planned_end_date DATE,
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE study_groups (
    study_id BIGINT NOT NULL,
    study_group_id INT NOT NULL,
    title VARCHAR,
    purpose TEXT,
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, study_group_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id)
);

CREATE INDEX study_groups_study_id ON study_groups(study_id);

CREATE TABLE observations (
    study_id BIGINT NOT NULL,
    observation_id INT NOT NULL,
    title VARCHAR,
    purpose TEXT,
    participant_info TEXT,
    type VARCHAR NOT NULL,
    study_group_id INT,
    properties JSONB,
    schedule JSONB,
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, observation_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id),
    FOREIGN KEY (study_id, study_group_id) REFERENCES study_groups(study_id, study_group_id)
);

CREATE INDEX observations_study_id ON observations(study_id);
CREATE INDEX observations_study_group_id ON observations(study_id, study_group_id);

CREATE TABLE interventions (
     study_id BIGINT NOT NULL,
     intervention_id INT NOT NULL,
     title VARCHAR,
     purpose TEXT,
     study_group_id INT,
     schedule JSONB,
     created TIMESTAMP NOT NULL DEFAULT now(),
     modified TIMESTAMP NOT NULL DEFAULT now(),

     PRIMARY KEY (study_id, intervention_id),
     FOREIGN KEY (study_id) REFERENCES studies(study_id),
     FOREIGN KEY (study_id, study_group_id) REFERENCES study_groups(study_id, study_group_id)
);

CREATE INDEX interventions_study_id ON interventions(study_id);
CREATE INDEX interventions_study_group_id ON interventions(study_id, study_group_id);

CREATE TABLE triggers (
      study_id BIGINT NOT NULL,
      intervention_id INT NOT NULL,
      type VARCHAR NOT NULL,
      properties JSONB,
      created TIMESTAMP NOT NULL DEFAULT now(),
      modified TIMESTAMP NOT NULL DEFAULT now(),

      PRIMARY KEY (study_id, intervention_id),
      FOREIGN KEY (study_id, intervention_id) REFERENCES interventions(study_id, intervention_id)
);

CREATE INDEX triggers_study_id ON triggers(study_id);

CREATE TABLE actions (
      study_id BIGINT NOT NULL,
      intervention_id INT NOT NULL,
      action_id INT NOT NULL,
      type VARCHAR NOT NULL,
      properties JSONB,
      created TIMESTAMP NOT NULL DEFAULT now(),
      modified TIMESTAMP NOT NULL DEFAULT now(),

      PRIMARY KEY (study_id, intervention_id, action_id),
      FOREIGN KEY (study_id, intervention_id) REFERENCES interventions(study_id, intervention_id)
);

CREATE INDEX actions_study_id ON actions(study_id);

CREATE TYPE participant_status AS ENUM ('new', 'registered');

CREATE TABLE participants (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    alias VARCHAR,
    study_group_id INT,
    status participant_status NOT NULL DEFAULT 'new',
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, participant_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id),
    FOREIGN KEY (study_id, study_group_id) REFERENCES study_groups(study_id, study_group_id)
);

CREATE INDEX participants_study_id ON participants(study_id);

CREATE TABLE participation_consents (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    consent_timestamp TIMESTAMP NOT NULL DEFAULT now(),
    accepted BOOLEAN NOT NULL,
    origin VARCHAR NOT NULL, -- e.g. deviceId of the mobile phone
    content_md5 VARCHAR NOT NULL,

    PRIMARY KEY (study_id, participant_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id)
);

CREATE TABLE observation_consents (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    observation_id INT NOT NULL,
    consent_timestamp TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, participant_id, observation_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id),
    FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id)
);

CREATE TABLE registration_tokens (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    token VARCHAR NOT NULL UNIQUE,

    PRIMARY KEY (study_id, participant_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id)
);

CREATE TABLE api_credentials (
    api_id VARCHAR NOT NULL PRIMARY KEY,
    api_secret VARCHAR NOT NULL,
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,

    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id)
);

CREATE VIEW gateway_participant_details(
    api_id,
    api_secret,
    study_id,
    participant_id
) AS SELECT ac.api_id,
   ac.api_secret,
   ac.study_id,
   ac.participant_id
FROM api_credentials ac;




