ALTER TABLE studies
    ADD COLUMN duration JSONB;

ALTER TABLE study_groups
    ADD COLUMN duration JSONB;
