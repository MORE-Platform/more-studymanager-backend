-- Observation resync cycles
--
-- A resync request is retried every resync_interval (urgent=1min, high=5min, normal=10min, low=30min)
-- from resync_start until resync_end. The storage gateway marks it synced once data was collected,
-- the studymanager then runs the data health check and deletes it. Requests past their end are deleted.
-- Existing requests become urgent-once (end = start).

ALTER TABLE observation_resync_requests
    ADD COLUMN resync_interval TEXT NOT NULL DEFAULT 'urgent'
        CHECK (resync_interval IN ('urgent', 'high', 'normal', 'low')),
    ADD COLUMN resync_start TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN resync_end TIMESTAMPTZ NOT NULL DEFAULT now(),
    ADD COLUMN synced BOOLEAN NOT NULL DEFAULT FALSE;
