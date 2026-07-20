-- record when the participant withdrew the consent
ALTER TABLE participation_consents
    ADD COLUMN consent_withdrawn TIMESTAMP DEFAULT NULL;
