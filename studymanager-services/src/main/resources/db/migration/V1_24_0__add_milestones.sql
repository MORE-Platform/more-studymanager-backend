-- Milestones
--
-- A milestone defines a named point in a study which an observation can leverage to schedule
-- relative to a participant's actual, individual point in time for that milestone.
-- Milestones are ordered per study via order_index; participant_milestones sets the concrete
-- date-time a given milestone occurs at for a given participant.

CREATE TABLE milestones (
    study_id BIGINT NOT NULL,
    milestone_id INT NOT NULL,
    name VARCHAR NOT NULL,
    order_index INT NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, milestone_id),
    FOREIGN KEY (study_id) REFERENCES studies(study_id) ON DELETE CASCADE
);

CREATE INDEX milestones_study_id ON milestones(study_id);

CREATE TABLE participant_milestones (
    study_id BIGINT NOT NULL,
    participant_id INT NOT NULL,
    milestone_id INT NOT NULL,
    participant_milestone_id INT NOT NULL,
    date_time TIMESTAMP NOT NULL,
    created TIMESTAMP NOT NULL DEFAULT now(),
    modified TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (study_id, participant_id, milestone_id),
    UNIQUE (study_id, participant_milestone_id),
    FOREIGN KEY (study_id, participant_id) REFERENCES participants(study_id, participant_id) ON DELETE CASCADE,
    FOREIGN KEY (study_id, milestone_id) REFERENCES milestones(study_id, milestone_id) ON DELETE CASCADE
);

CREATE INDEX participant_milestones_study_participant ON participant_milestones(study_id, participant_id);

-- Study observations can be assigned [0..1] to a milestone
ALTER TABLE observations
    ADD COLUMN milestone_id INT,

    ADD FOREIGN KEY (study_id, milestone_id) REFERENCES milestones(study_id, milestone_id) ON DELETE SET NULL (milestone_id);

CREATE INDEX observations_milestone_id ON observations(study_id, milestone_id);
