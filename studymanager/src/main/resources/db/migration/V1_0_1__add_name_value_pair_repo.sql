CREATE TABLE nvpairs (
    issuer varchar,
    name varchar,
    value bytea NOT NULL,

    PRIMARY KEY (issuer, name)
)
