ALTER TABLE observation_api_tokens
    ALTER COLUMN token_id TYPE INT,
    ALTER COLUMN token_id DROP DEFAULT;
DROP SEQUENCE observation_api_tokens_token_id_seq;

