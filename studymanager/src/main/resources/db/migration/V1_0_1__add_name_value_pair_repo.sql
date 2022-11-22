CREATE TABLE nvpairs (
    issuer varchar,
    name varchar,
    value text NOT NULL,

    PRIMARY KEY (issuer, name)
)
