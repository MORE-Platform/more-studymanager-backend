CREATE TYPE notifications_type AS ENUM ('text', 'data');

CREATE TABLE notifications (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    msg_id VARCHAR,
    type notifications_type NOT NULL,
    data TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT NOW(),

    PRIMARY KEY (study_id, participant_id, msg_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id) ON DELETE CASCADE
);
