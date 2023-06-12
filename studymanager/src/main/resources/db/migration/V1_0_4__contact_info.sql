ALTER TABLE studies
    ADD COLUMN institute VARCHAR,
    ADD COLUMN contact_person VARCHAR,
    ADD COLUMN contact_email VARCHAR,
    ADD COLUMN contact_phone VARCHAR;

UPDATE studies
    SET contact_person = 'pending', contact_email = 'pending';

ALTER TABLE studies
    ALTER COLUMN contact_person SET NOT NULL,
    ALTER COLUMN contact_email SET NOT NULL;
