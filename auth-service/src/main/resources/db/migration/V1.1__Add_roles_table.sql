/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-59)
 *
 */
CREATE TABLE auth.ROLES(
        id INT NOT NULL AUTO_INCREMENT,
        name VARCHAR(2048) NOT NULL UNIQUE,
        parent_id int NULL,
        FOREIGN KEY(parent_id) REFERENCES auth.ROLES(id),
        PRIMARY KEY(id)
);