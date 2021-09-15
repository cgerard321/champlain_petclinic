CREATE TABLE auth.USERS_ROLES(
    id INT NOT NULL AUTO_INCREMENT,
    user_id INT NOT NULL,
    role_id INT NOT NULL,
    FOREIGN KEY(user_id)
        REFERENCES auth.USERS(id)
         ON DELETE CASCADE,
    FOREIGN KEY(role_id)
         REFERENCES auth.ROLES(ID)
         ON DELETE CASCADE,
    PRIMARY KEY(id)
);