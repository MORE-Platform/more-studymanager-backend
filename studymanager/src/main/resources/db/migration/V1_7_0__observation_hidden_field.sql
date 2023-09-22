ALTER TABLE observations
    ADD COLUMN hidden BOOLEAN;

UPDATE observations
SET hidden = FALSE;

ALTER TABLE observations
    ALTER COLUMN hidden SET NOT NULL;
