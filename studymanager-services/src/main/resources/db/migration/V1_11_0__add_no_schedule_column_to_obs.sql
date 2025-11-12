ALTER TABLE observations
    ADD COLUMN no_schedule BOOLEAN;

UPDATE observations
SET no_schedule = FALSE;

ALTER TABLE observations
    ALTER COLUMN no_schedule SET NOT NULL;
