-- Observation resync properties
--
-- Free-form details for the storage gateway, e.g. why the data health check requested the resync.

ALTER TABLE observation_resync_requests
    ADD COLUMN properties JSONB NOT NULL DEFAULT '{}'::jsonb;
