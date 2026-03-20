ALTER TABLE studies
    RENAME COLUMN participant_portal_access TO application_access;
ALTER TABLE studies ALTER COLUMN application_access DROP DEFAULT;
ALTER TABLE studies ALTER COLUMN application_access TYPE TEXT [] USING
    CASE WHEN application_access THEN ARRAY['PARTICIPANT_PORTAL'] ELSE ARRAY[]:: TEXT []
END;
ALTER TABLE studies ALTER COLUMN application_access SET DEFAULT ARRAY[]:: TEXT [];
