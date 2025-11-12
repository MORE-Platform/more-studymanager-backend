-- Store Push-Notification Tokens
CREATE TABLE push_notifications_token(
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    service VARCHAR NOT NULL,
    token VARCHAR NOT NULL,

    created TIMESTAMP NOT NULL DEFAULT now(),
    updated TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, participant_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants ON DELETE CASCADE
);

