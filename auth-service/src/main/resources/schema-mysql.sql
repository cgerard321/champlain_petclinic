USE `auth-db`;
create table if not exists users (
         id INT AUTO_INCREMENT,
         user_id VARCHAR(36) UNIQUE,
         email VARCHAR(2048) NOT NULL UNIQUE,
         username VARCHAR(2048) NOT NULL UNIQUE,
         password VARCHAR(2048) NOT NULL,
         verified BOOL,
         is_disabled BOOL DEFAULT FALSE,
         PRIMARY KEY(id)
);

create table if not exists roles (
        id INT AUTO_INCREMENT,
        name VARCHAR(2048) NOT NULL UNIQUE,
        PRIMARY KEY (id)
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
        user_identifier INT UNIQUE,
        expiry_date DATETIME,
        FOREIGN KEY(user_identifier) REFERENCES users(id)
);