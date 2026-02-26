CREATE TABLE intervention_api_tokens (
    study_id BIGINT NOT NULL,
    intervention_id INT NOT NULL,
    token_id SERIAL NOT NULL,
    token_label VARCHAR NOT NULL,
    token VARCHAR UNIQUE NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),
    PRIMARY KEY (study_id, intervention_id, token_id),
    FOREIGN KEY (study_id, intervention_id) REFERENCES interventions(study_id, intervention_id) ON DELETE CASCADE
);
