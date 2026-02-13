-- Add reminder column to observations table

ALTER TABLE observations
    ADD COLUMN reminder BOOLEAN NOT NULL DEFAULT FALSE;