/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-59)
 *
 */
CREATE SCHEMA IF NOT EXISTS auth;

CREATE TABLE auth.users(
    id INT NOT NULL AUTO_INCREMENT,
    email VARCHAR(2048) NOT NULL UNIQUE,
    username VARCHAR(2048) NOT NULL,
    password VARCHAR(2048) NOT NULL,
    PRIMARY KEY(id)
);