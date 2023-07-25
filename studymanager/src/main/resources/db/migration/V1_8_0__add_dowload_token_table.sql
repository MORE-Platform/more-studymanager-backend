CREATE TABLE download_tokens (
     token VARCHAR PRIMARY KEY,
     study_id VARCHAR,
     filename VARCHAR,
     expiry TIMESTAMP NOT NULL DEFAULT NOW() + INTERVAL '10 minutes'
);
