ALTER TABLE studies
    ADD COLUMN institute VARCHAR,
    ADD COLUMN contact_person VARCHAR NOT NULL DEFAULT 'pending',
    ADD COLUMN contact_email VARCHAR NOT NULL DEFAULT  'pending',
    ADD COLUMN contact_phone VARCHAR;