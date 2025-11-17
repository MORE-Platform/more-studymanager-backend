-- create table for storing audit_logs
CREATE TYPE audit_action_state AS ENUM ('success', 'redirect', 'error', 'unknown');

CREATE TABLE audit_logs (
     id BIGSERIAL PRIMARY KEY,
     user_id VARCHAR NOT NULL,
     study_id BIGINT NOT NULL,
     action VARCHAR NOT NULL,
     timestamp TIMESTAMP NOT NULL,
     state audit_action_state NOT NULL default 'unknown',
     resource VARCHAR,
     details TEXT,
     created TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX audit_by_time ON audit_logs (timestamp);
CREATE INDEX audit_by_state ON audit_logs (state, timestamp);
CREATE INDEX audit_by_study ON audit_logs (study_id, timestamp);
CREATE INDEX audit_by_user ON audit_logs (study_id, timestamp);
CREATE INDEX audit_by_action ON audit_logs (action, timestamp);
