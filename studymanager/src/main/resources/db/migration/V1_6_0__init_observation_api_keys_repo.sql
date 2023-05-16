CREATE TABLE observation_api_tokens (
    study_id BIGINT NOT NULL,
    observation_id INT NOT NULL,
    token_id INT NOT NULL DEFAULT nextval(api_token_id_seq),
    token_label VARCHAR NOT NULL,
    token VARCHAR UNIQUE NOT NULL,

    created TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, observation_id, token_id),
    FOREIGN KEY (study_id, observation_id) REFERENCES observations(study_id, observation_id) ON DELETE CASCADE
);

CREATE SEQUENCE api_token_id_seq
START WITH 1
INCREMENT BY 1
MINVALUE 1
NO MAXVALUE
CACHE 1;