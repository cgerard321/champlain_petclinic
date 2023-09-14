/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-59)
 *
 */
CREATE TABLE auth.roles (
                            id INT AUTO_INCREMENT,
                            name VARCHAR(2048) NOT NULL UNIQUE,
                            parent_id INT NULL,
                            PRIMARY KEY (id),
                            FOREIGN KEY (parent_id) REFERENCES auth.roles (id)
);

INSERT INTO auth.roles (id, name) VALUES (1, 'ADMIN');