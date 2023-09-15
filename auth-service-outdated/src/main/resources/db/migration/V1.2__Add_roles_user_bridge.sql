/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-59)
 *
 */
CREATE TABLE auth.users_roles(
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY(user_id)
        REFERENCES auth.users(id),
    FOREIGN KEY(role_id)
         REFERENCES auth.roles(id),
    PRIMARY KEY(id)
);