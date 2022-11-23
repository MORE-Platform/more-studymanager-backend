CREATE TABLE users(
    user_id     VARCHAR,
    name        VARCHAR,
    institution VARCHAR,
    email       VARCHAR,
    inserted    TIMESTAMP NOT NULL DEFAULT now(),
    updated     TIMESTAMP NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id)
);

CREATE INDEX users_name ON users (name);
CREATE INDEX users_institution ON users (institution);
CREATE INDEX users_email ON users (email);

CREATE TABLE study_acl(
    study_id   BIGINT  NOT NULL,
    user_id    VARCHAR NOT NULL,
    user_role  VARCHAR NOT NULL,
    created    TIMESTAMP DEFAULT now(),
    creator_id VARCHAR,

    PRIMARY KEY (study_id, user_id, user_role),
    FOREIGN KEY (study_id) REFERENCES studies ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE,
    FOREIGN KEY (creator_id) REFERENCES users (user_id) ON DELETE SET NULL
);

