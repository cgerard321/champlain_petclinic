CREATE TABLE IF NOT EXISTS users
(
    id
    VARCHAR
(
    36
) NOT NULL PRIMARY KEY,
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    pass_hash VARBINARY
(
    255
) NOT NULL,
    display_name VARCHAR
(
    120
) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS roles
(
    id
    VARCHAR
(
    36
) NOT NULL PRIMARY KEY,
    code VARCHAR
(
    64
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS user_roles
(
    user_id
    VARCHAR
(
    36
) NOT NULL,
    role_id VARCHAR
(
    36
) NOT NULL,
    PRIMARY KEY
(
    user_id,
    role_id
),
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
) ON DELETE CASCADE,
    FOREIGN KEY
(
    role_id
) REFERENCES roles
(
    id
)
  ON DELETE CASCADE
    );

CREATE TABLE IF NOT EXISTS sessions
(
    id
    VARCHAR
(
    36
) NOT NULL PRIMARY KEY,
    user_id VARCHAR
(
    36
) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at DATETIME NOT NULL,
    FOREIGN KEY
(
    user_id
) REFERENCES users
(
    id
) ON DELETE CASCADE
    );
