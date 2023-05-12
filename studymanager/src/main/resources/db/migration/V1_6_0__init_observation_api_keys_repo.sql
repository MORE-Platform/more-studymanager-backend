CREATE TABLE observation_api_tokens (
    study_id BIGINT NOT NULL,
    observation_id INT NOT NULL,
    token_id INT NOT NULL,
    token_label VARCHAR NOT NULL,
    token VARCHAR UNIQUE NOT NULL,

    created TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, observation_id, token_id),
    FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id) ON DELETE CASCADE
);