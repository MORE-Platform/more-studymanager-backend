ALTER TABLE interventions
    ADD COLUMN milestone_id INT,

    ADD FOREIGN KEY (study_id, milestone_id) REFERENCES milestones(study_id, milestone_id) ON DELETE SET NULL (milestone_id);

CREATE INDEX interventions_milestone_id ON interventions(study_id, milestone_id);
