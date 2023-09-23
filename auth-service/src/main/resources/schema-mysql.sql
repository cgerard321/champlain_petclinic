USE `auth-db`;
create table if not exists users (
                                     id INT AUTO_INCREMENT,
                                     email VARCHAR(2048) NOT NULL UNIQUE,
                                     username VARCHAR(2048) NOT NULL,
                                     password VARCHAR(2048) NOT NULL,
                                     verified BOOL,
                                     PRIMARY KEY(id)
);

create table if not exists roles (
                       id INT AUTO_INCREMENT,
                       name VARCHAR(2048) NOT NULL UNIQUE,
                       parent_id INT NULL,
                       PRIMARY KEY (id),
                       FOREIGN KEY (parent_id) REFERENCES roles (id)
);


create table if not exists users_roles(
                            id INT AUTO_INCREMENT,
                            user_id INT NOT NULL,
                            role_id INT NOT NULL,
                            FOREIGN KEY(user_id)
                                REFERENCES users(id),
                            FOREIGN KEY(role_id)
                                REFERENCES roles(id),
                            PRIMARY KEY(id)
);


create table if not exists reset_password_token (
                                                    id INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                                    token VARCHAR(100),
    user_identifier VARCHAR(36) UNIQUE,
    expiry_date DATETIME
    );